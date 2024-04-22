package app.thecity.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import app.thecity.R;
import app.thecity.data.Constant;
import app.thecity.model.Place;
import app.thecity.utils.Tools;

public class GeneralInfoCard extends FrameLayout {

    private Place place;

    private int width, height;

    public GeneralInfoCard(@NonNull Context context) {
        super(context);
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void update() {
        Context context = getContext();

        int density = (int) getResources().getDisplayMetrics().density;

        LayoutParams layoutParams = new LayoutParams(
                width * density,
                height * density
        );
        setLayoutParams(layoutParams);
        layoutParams.rightMargin = 12 * density;

        GradientDrawable background = new GradientDrawable();
        background.setColor(getResources().getColor(R.color.white));
        background.setCornerRadius(12 * density);
        setBackground(background);
        setClipToOutline(true);
        setElevation(2 * density);
        setTranslationZ(10 * density);

        ImageView imageView = new ImageView(context);
        LayoutParams imageViewLP = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        imageView.setLayoutParams(imageViewLP);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        Tools.displayImage(context, imageView, Constant.getURLimgPlace(place.image));

        FlexboxLayout infoLayout = new FlexboxLayout(context);
        FrameLayout.LayoutParams infoLP = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) ((height * 2.0/5.0) * density)
        );
        infoLP.gravity = Gravity.BOTTOM;

        infoLayout.setLayoutParams(infoLP);
        infoLayout.setFlexDirection(FlexDirection.COLUMN);
        infoLayout.setJustifyContent(JustifyContent.SPACE_EVENLY);
        infoLayout.setPadding(10 * density, 10 * density, 10 * density, 10 * density);

        GradientDrawable infoBackground = new GradientDrawable();
        infoBackground.setCornerRadius(12 * density);
        infoBackground.setColor(getResources().getColor(R.color.white));
        infoLayout.setBackground(infoBackground);

        LinearLayout ratingLayout = new LinearLayout(context);
        ratingLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView starIcon = new ImageView(context);
        LinearLayout.LayoutParams starIconLP = new LinearLayout.LayoutParams(
                15 * density,
                15 * density
        );
        starIcon.setLayoutParams(starIconLP);
        starIcon.setImageResource(R.drawable.ic_star);

        TextView ratingScore = new TextView(context);
        ratingScore.setText(place.rating + "");
        ratingScore.setTextSize(12);

        TextView ratingCount = new TextView(context);
        ratingCount.setText("(" + place.ratingCount + ")");
        ratingCount.setTextSize(12);
        ratingCount.setPadding(8 * density, 0, 0, 0);
        ratingCount.setTextColor(getResources().getColor(R.color.grey_mdark));

        ratingLayout.addView(starIcon);
        ratingLayout.addView(ratingScore);
        ratingLayout.addView(ratingCount);

        LinearLayout desInfoLayout = new LinearLayout(context);
        desInfoLayout.setOrientation(LinearLayout.VERTICAL);

        TextView _title = new TextView(context);
        _title.setText(R.string.destination_title);
        _title.setTextSize(12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            _title.setTypeface(Typeface.create(null, 700, false));
        }

        TextView distance = new TextView(context);
        distance.setText(getResources().getString(R.string.distance_remaining)
                .replaceAll("%distance%", "" + ( Math.round(place.distance * 10)/10 ))
                .replaceAll("%place%", place.name));
        distance.setTextSize(9);
        distance.setLines(1);
        distance.setEllipsize(TextUtils.TruncateAt.END);

        desInfoLayout.addView(_title);
        desInfoLayout.addView(distance);

        LinearLayout destinationLayout = new LinearLayout(context);
        destinationLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView destinationIcon = new ImageView(context);
        LinearLayout.LayoutParams destinationIconLP = new LinearLayout.LayoutParams(
                10 * density, 10 * density
        );
        destinationIcon.setLayoutParams(destinationIconLP);
        destinationIcon.setImageResource(R.drawable.ic_yellow_marker);

        TextView destination = new TextView(context);
        destination.setPadding(4 * density, 0, 0, 0);
        destination.setText(place.name);
        destination.setTextSize(9);
        destination.setTextColor(getResources().getColor(R.color.grey_mdark));

        destinationLayout.addView(destinationIcon);
        destinationLayout.addView(destination);

        infoLayout.addView(ratingLayout);
        infoLayout.addView(desInfoLayout);
        infoLayout.addView(destinationLayout);

        addView(imageView);
        addView(infoLayout);
    }
}
