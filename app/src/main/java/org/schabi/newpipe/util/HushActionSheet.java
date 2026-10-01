package org.schabi.newpipe.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.schabi.newpipe.R;

import java.util.List;

/**
 * One bottom sheet for choice menus: a title, optional subtitle, and 56dp rows.
 */
public final class HushActionSheet {

    public static final class Action {
        @DrawableRes public final int icon;
        @NonNull public final CharSequence label;
        @Nullable public final CharSequence detail;
        public final boolean selected;
        public final boolean dismissOnClick;
        @NonNull public final Runnable onClick;

        public Action(@DrawableRes final int icon,
                      @NonNull final CharSequence label,
                      @Nullable final CharSequence detail,
                      final boolean selected,
                      @NonNull final Runnable onClick) {
            this(icon, label, detail, selected, true, onClick);
        }

        public Action(@DrawableRes final int icon,
                      @NonNull final CharSequence label,
                      @Nullable final CharSequence detail,
                      final boolean selected,
                      final boolean dismissOnClick,
                      @NonNull final Runnable onClick) {
            this.icon = icon;
            this.label = label;
            this.detail = detail;
            this.selected = selected;
            this.dismissOnClick = dismissOnClick;
            this.onClick = onClick;
        }

    }

    private HushActionSheet() {
    }

    @NonNull
    public static BottomSheetDialog show(@NonNull final Context context,
                                          @Nullable final CharSequence title,
                                          @Nullable final CharSequence subtitle,
                                          @NonNull final List<Action> actions) {
        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(build(context, title, subtitle, actions, dialog));
        dialog.setOnShowListener(shown -> style(dialog, context));
        dialog.show();
        return dialog;
    }

    @NonNull
    public static View build(@NonNull final Context context,
                             @Nullable final CharSequence title,
                             @Nullable final CharSequence subtitle,
                             @NonNull final List<Action> actions,
                             @NonNull final BottomSheetDialog dialog) {
        final View root = LayoutInflater.from(context).inflate(R.layout.sheet_actions, null);
        final TextView titleView = root.findViewById(R.id.sheet_title);
        final TextView subtitleView = root.findViewById(R.id.sheet_subtitle);
        final LinearLayout list = root.findViewById(R.id.sheet_list);
        if (title == null || title.length() == 0) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }
        if (subtitle == null || subtitle.length() == 0) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(View.VISIBLE);
        }
        for (final Action action : actions) {
            final View row = LayoutInflater.from(context)
                    .inflate(R.layout.row_sheet_action, list, false);
            final ImageView icon = row.findViewById(R.id.sheet_row_icon);
            final TextView label = row.findViewById(R.id.sheet_row_label);
            final TextView detail = row.findViewById(R.id.sheet_row_detail);
            final ImageView check = row.findViewById(R.id.sheet_row_check);
            label.setText(action.label);
            if (action.icon == 0) {
                icon.setVisibility(View.GONE);
            } else {
                icon.setImageDrawable(org.schabi.newpipe.hush.ui.HushIcons.drawable(context,action.icon));
            }
            if (action.detail == null || action.detail.length() == 0) {
                detail.setVisibility(View.GONE);
            } else {
                detail.setText(action.detail);
                detail.setVisibility(View.VISIBLE);
            }
            check.setVisibility(action.selected ? View.VISIBLE : View.GONE);
            row.setOnClickListener(v -> {
                if (action.dismissOnClick) {
                    dialog.dismiss();
                }
                action.onClick.run();
            });
            list.addView(row);
        }
        return root;
    }

    public static void style(@NonNull final BottomSheetDialog dialog,
                             @NonNull final Context context) {
        final View sheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) {
            return;
        }
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(
                com.google.android.material.R.attr.colorSurfaceContainerLow, value, true);
        final float radius = 28f * context.getResources().getDisplayMetrics().density;
        final MaterialShapeDrawable background = new MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                        .setTopLeftCornerSize(radius)
                        .setTopRightCornerSize(radius)
                        .build());
        background.setFillColor(ColorStateList.valueOf(value.data));
        sheet.setBackground(background);
        final com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior =
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet);
        behavior.setFitToContents(true);
        behavior.setSkipCollapsed(true);
        // The default 16:9 peek calculation leaves only a title visible in wide windows.
        // Expand choice lists immediately and bound their scroll viewport by actual insets.
        sheet.post(() -> {
            if (!dialog.isShowing() || dialog.getWindow() == null) return;
            final View decor = dialog.getWindow().getDecorView();
            final androidx.core.view.WindowInsetsCompat insets =
                    androidx.core.view.ViewCompat.getRootWindowInsets(decor);
            final androidx.core.graphics.Insets safe = insets == null
                    ? androidx.core.graphics.Insets.NONE : insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()
                            | androidx.core.view.WindowInsetsCompat.Type.displayCutout());
            final int height = decor.getHeight() - safe.top - safe.bottom
                    - org.schabi.newpipe.hush.ui.HushUi.dp(context, 16);
            if (height > 0) behavior.setMaxHeight(height);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        });
    }
}
