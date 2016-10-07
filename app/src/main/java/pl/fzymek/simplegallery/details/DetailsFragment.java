package pl.fzymek.simplegallery.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.fzymek.gettyimagesmodel.gettyimages.Image;
import pl.fzymek.simplegallery.R;

//TODO: Use MVP here
public class DetailsFragment extends Fragment {

    private final static String IMAGE_ARG = "image";

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private Image img;

    public static DetailsFragment newInstance(Image img) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(IMAGE_ARG, img);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        img = getArguments().getParcelable(IMAGE_ARG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        getAppCompatActivity().setSupportActionBar(toolbar);
        getAppCompatActivity().getSupportActionBar().setHomeButtonEnabled(false);
        getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbar.setTitle(img.getTitle());
        ViewCompat.setTransitionName(image, "transition_"+img.getId());
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        collapsingToolbar.setTitle(img.getTitle());
        Glide.with(view.getContext()).load(img.getDisplayByType(Image.DisplaySizeType.PREVIEW).getUri()).into(image);
    }

    AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
