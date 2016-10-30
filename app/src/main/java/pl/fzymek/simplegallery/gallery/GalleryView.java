package pl.fzymek.simplegallery.gallery;

import android.support.annotation.Nullable;

import net.grandcentrix.thirtyinch.TiView;
import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThread;
import net.grandcentrix.thirtyinch.distinctuntilchanged.DistinctUntilChanged;

import java.util.List;

import pl.fzymek.gettyimagesmodel.gettyimages.Image;


public interface GalleryView extends TiView {

    @DistinctUntilChanged
    @CallOnMainThread
    void showLoadingIndicator(final boolean loading);

    @CallOnMainThread
    void showError(@Nullable Throwable error);

    @DistinctUntilChanged
    @CallOnMainThread
    void showGallery(List<Image> images);

}
