package pl.fzymek.simplegallery.gallery;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import net.grandcentrix.thirtyinch.TiFragment;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChangedInterceptor;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.fzymek.gettyimagesmodel.gettyimages.Image;
import pl.fzymek.simplegallery.R;
import pl.fzymek.simplegallery.details.DetailsFragment;
import pl.fzymek.simplegallery.util.ImageTransition;
import pl.fzymek.simplegallery.util.SpaceDecoration;
import timber.log.Timber;

public class GalleryFragment extends TiFragment<GalleryPresenter, GalleryView> implements GalleryView, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.errorView)
    SwipeRefreshLayout errorView;
    @BindView(R.id.contentView)
    SwipeRefreshLayout contentView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    GalleryAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GalleryAdapter();
        adapter.setListener((v, image) -> {
            Timber.d("clicked: %s", v);
            openDetails(v, image);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        contentView.setOnRefreshListener(this);
        errorView.setOnRefreshListener(this);

        Context context = view.getContext();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new LinearLayoutManager(context);
        } else {
            layoutManager = new GridLayoutManager(context, 3);
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SpaceDecoration());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        // manually clear DistinctUntilChanged cache
        // workaround for issue: https://github.com/grandcentrix/ThirtyInch/issues/29
        // not required when @DistinctUntilChanged isn't used. Only wrong Fragments, not Activity
        final DistinctUntilChangedInterceptor distinctUntilChangedInterceptor = (DistinctUntilChangedInterceptor)
                getInterceptors(interceptor -> interceptor instanceof DistinctUntilChangedInterceptor).get(0);
        distinctUntilChangedInterceptor.clearCache(this);
    }

    void openDetails(View view, Image image) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        DetailsFragment fragment = DetailsFragment.newInstance(image);
        ImageTransition sharedElementEnterTransition = new ImageTransition();
        fragment.setSharedElementEnterTransition(sharedElementEnterTransition);
//        setSharedElementReturnTransition(sharedElementEnterTransition);
        fm.beginTransaction()
                .replace(R.id.content, fragment)
                .addSharedElement(view.findViewById(R.id.image), "transition_" + image.getId())
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @NonNull
    @Override
    public GalleryPresenter providePresenter() {
        Timber.d("providePresenter");
        return new GalleryPresenter();
    }

    @Override
    public void showLoadingIndicator(boolean loading) {
        Timber.d("showLoadingIndicator() called with: loading = [" + loading + "]");
        if (loading) {
            if (contentView.isRefreshing() || errorView.isRefreshing()) {
                //already showing PTR loading indicator
                return;
            }
        } else {
            contentView.setRefreshing(false);
            errorView.setRefreshing(false);
        }

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showError(Throwable err) {
        Timber.d("showError " + err);
        errorView.setVisibility(err == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void showGallery(List<Image> images) {
        Timber.d("showGallery images:" + images.size() + " " + images.hashCode());
        adapter.setData(images);
        contentView.setVisibility(images.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        Timber.v("onRefresh() called");
        getPresenter().onRefresh();
    }
}

