package vn.edu.usth.stockdashboard.view;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import vn.edu.usth.stockdashboard.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomMarkerView extends MarkerView {
    private TextView tvPrice;
    private TextView tvDate;
    private SimpleDateFormat dateFormat;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvPrice = findViewById(R.id.tvMarkerPrice);
//        tvDate = findViewById(R.id.tvMarkerDate);
//        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvPrice.setText(String.format(Locale.US, "$%.2f", e.getY()));
//        tvDate.setText(dateFormat.format(new Date((long) e.getX())));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}