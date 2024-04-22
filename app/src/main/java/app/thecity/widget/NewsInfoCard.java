package app.thecity.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import app.thecity.R;
import app.thecity.data.Constant;
import app.thecity.model.NewsInfo;
import app.thecity.utils.Tools;

public class NewsInfoCard extends FrameLayout {
    private NewsInfo news;

    public NewsInfoCard(@NonNull Context context) {
        super(context);
    }

    public void setNews(NewsInfo news) {
        this.news = news;
    }

    public void update() {
        Context context = getContext();
        NewsInfo newsInfo = news;

        int density = (int) getResources().getDisplayMetrics().density;

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                230 * density
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
        Tools.displayImage(context, imageView, Constant.getURLimgNews(newsInfo.image));

        FlexboxLayout infoLayout = new FlexboxLayout(context);
        LayoutParams infoLP = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) ((230 * 2.0/5.0) * density)
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

        TextView newsTitle = new TextView(context);
        newsTitle.setText(newsInfo.title);
        newsTitle.setTextSize(16);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            newsTitle.setTypeface(Typeface.create(null, 500, false));
        }

        TextView description = new TextView(context);
        description.setText(newsInfo.brief_content);
        description.setTextSize(10);
        description.setTextColor(Color.parseColor("#A6A6A6"));
        description.setEllipsize(TextUtils.TruncateAt.END);
        description.setPadding(0, 10 * density, 0, 0);

        infoLayout.addView(newsTitle);
        infoLayout.addView(description);

        addView(imageView);
        addView(infoLayout);
    }
}
