package pl.fzymek.simplegallery.gallery;

import net.grandcentrix.thirtyinch.TiConfiguration;
import net.grandcentrix.thirtyinch.TiPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pl.fzymek.gettyimagesmodel.gettyimages.GettySearchResult;
import pl.fzymek.gettyimagesmodel.gettyimages.Image;
import pl.fzymek.simplegallery.config.Config;
import pl.fzymek.simplegallery.network.GettyImagesService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


class GalleryPresenter extends TiPresenter<GalleryView> {

    private final static TiConfiguration PRESENTER_CONFIGURATION = new TiConfiguration.Builder()
            .setRetainPresenterEnabled(true)
            .build();

    private Subscription mRefreshSubscription;

    private GettyImagesService service;
    private GettySearchResult result;
    private Throwable error;
    private boolean loading = false;

    GalleryPresenter() {
        super(PRESENTER_CONFIGURATION);
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Config.GETTYIMAGES_API_URL)
                .build();


        service = retrofit.create(GettyImagesService.class);
    }

    public void onRefresh() {
        String phrase = Config.QUERIES[new Random().nextInt(Config.QUERIES.length)];
        loadData(phrase);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        Timber.d("onCreate");

        String phrase = Config.QUERIES[new Random().nextInt(Config.QUERIES.length)];
        loadData(phrase);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        cancelRequest();
    }

    @Override
    protected void onSleep() {
        super.onSleep();
        Timber.d("onSleep");
    }

    @Override
    protected void onWakeUp() {
        super.onWakeUp();
        Timber.d("onWakeUp");
        updateView();
    }


    public void loadData(String phrase) {
        Timber.d("loadData() called with: phrase = [" + phrase + "]");
        cancelRequest();

        clearData();

        mRefreshSubscription = service.getImages(phrase)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> setLoading(true))
                .doAfterTerminate(() -> {
                    setLoading(false);
                    updateView();
                })
                .subscribe(
                        result -> {
                            GalleryPresenter.this.result = result;
                            Timber.d("loaded images: " + result.getImages().size());
                        },
                        error -> {
                            GalleryPresenter.this.error = error;
                            Timber.d("got error: " + error);
                        }
                );
    }

    private void updateView() {
        final GalleryView view = getView();
        if (view == null) {
            Timber.d("not updating view. view == null");
            return;
        }
        Timber.d("updateView");

        view.showError(error);

        view.showLoadingIndicator(loading);

        List<Image> images = new ArrayList<>();
        if (result != null) {
            images = result.getImages();
        }
        view.showGallery(images);
    }

    private void clearData() {
        Timber.d("clear data");
        result = null;
        error = null;
        updateView();
    }

    private void setLoading(boolean loading) {
        this.loading = loading;
        updateView();
    }

    private void cancelRequest() {
        if (mRefreshSubscription != null && !mRefreshSubscription.isUnsubscribed()) {
            mRefreshSubscription.unsubscribe();
        }
    }
}
