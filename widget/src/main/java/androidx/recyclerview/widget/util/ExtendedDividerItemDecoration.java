/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.recyclerview.widget.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class ExtendedDividerItemDecoration extends RecyclerView.ItemDecoration {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL, VERTICAL})
    private @interface Orientation {}

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    private static final int[] ATTRS = new int[]{ android.R.attr.listDivider };

    private Drawable divider;

    private ColorStateList drawableTintList = null;
    private PorterDuff.Mode drawableTintMode = null;
    private boolean hasDrawableTint = false;
    private boolean hasDrawableTintMode = false;

    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    private int orientation;

    private final Rect bounds = new Rect();

    private int marginLeft = 0;
    private int marginTop = 0;
    private int marginRight = 0;
    private int marginBottom = 0;

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     * {@link LinearLayoutManager}.
     *
     * @param context Current context, it will be used to access resources.
     * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    public ExtendedDividerItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        divider = a.getDrawable(0);
        if (divider == null) {
            Timber.w("@android:attr/listDivider was not set in the theme used for this "
                    + "DividerItemDecoration. Please set that attribute all call setDrawable()");
        }
        a.recycle();
        setOrientation(orientation);
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        this.orientation = orientation;
    }

    /**
     * Sets the {@link Drawable} for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    public void setDrawable(@NonNull Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        divider = drawable;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null || divider == null) {
            return;
        }
        if (orientation == VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        int left;
        int right;
        //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        // apply margin
        left += marginLeft;
        right -= marginRight;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, bounds);
            int bottom = bounds.bottom + Math.round(child.getTranslationY());
            int top = bottom - divider.getIntrinsicHeight();

            // apply margin
            top += marginTop;
            bottom -= marginBottom;

            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
        canvas.restore();
    }

    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        int top;
        int bottom;
        //noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        // apply margin
        top += marginTop;
        bottom -= marginBottom;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, bounds);
            int right = bounds.right + Math.round(child.getTranslationX());
            int left = right - divider.getIntrinsicWidth();

            // apply margin
            left += marginLeft;
            right -= marginRight;

            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (divider == null) {
            outRect.set(0, 0, 0, 0);
            return;
        }
        if (orientation == VERTICAL) {
            outRect.set(0, 0, 0, divider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, divider.getIntrinsicWidth(), 0);
        }
    }

    /* The extended functions */

    /**
     * Applies a tint to the image drawable. Does not modify the current tint
     * mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
     * <p>
     * Subsequent calls to {@link #setDrawable(Drawable)} will automatically
     * mutate the drawable and apply the specified tint and tint mode using
     * {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param tint the tint to apply, may be {@code null} to clear tint
     *
     * @see Drawable#setTintList(ColorStateList)
     */
    public void setTintList(@Nullable ColorStateList tint) {
        drawableTintList = tint;
        hasDrawableTint = true;

        applyImageTint();
    }

    /**
     * Specifies the blending mode used to apply the tint specified by
     * {@link #setTintList(ColorStateList)}} to the image drawable. The default
     * mode is {@link PorterDuff.Mode#SRC_IN}.
     *
     * @param tintMode the blending mode used to apply the tint, may be
     *                 {@code null} to clear tint
     * @see Drawable#setTintMode(PorterDuff.Mode)
     */
    public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
        drawableTintMode = tintMode;
        hasDrawableTintMode = true;

        applyImageTint();
    }

    private void applyImageTint() {
        if (divider != null && (hasDrawableTint || hasDrawableTintMode)) {
            divider = divider.mutate();

            if (hasDrawableTint) {
                divider.setTintList(drawableTintList);
            }

            if (hasDrawableTintMode) {
                divider.setTintMode(drawableTintMode);
            }
        }
    }

    /**
     * Sets the margins, in pixels.
     * Margin values should be positive.
     *
     * @param left the left margin size
     * @param top the top margin size
     * @param right the right margin size
     * @param bottom the bottom margin size
     */
    public void setMargins(int left, int top, int right, int bottom) {
        marginLeft = left;
        marginTop = top;
        marginRight = right;
        marginBottom = bottom;
    }

    public static class Builder {

        private Context mContext;
        private boolean mIsVertical = true;

        private Drawable mDivider = null;
        private ColorStateList mDrawableTintList = null;
        private PorterDuff.Mode mDrawableTintMode = null;

        private Rect mMargins = null;
        private int mMarginSize = -1;
        private int mMarginResId = 0;

        public Builder(@NonNull Context context) {
            mContext = context;
        }

        public Builder vertically() {
            mIsVertical = true;
            return this;
        }

        public Builder horizontally() {
            mIsVertical = false;
            return this;
        }

        public Builder setDrawable(Drawable dividerDrawable) {
            mDivider = dividerDrawable;
            return this;
        }

        public Builder setTint(@ColorInt int tintColor) {
            mDrawableTintList = ColorStateList.valueOf(tintColor);
            return this;
        }

        public Builder setTintList(@Nullable ColorStateList tint) {
            mDrawableTintList = tint;
            return this;
        }

        public Builder setTintMode(@Nullable PorterDuff.Mode tintMode) {
            mDrawableTintMode = tintMode;
            return this;
        }

        public Builder setMargin(@IntRange(from=0) int size) {
            mMarginSize = size;
            return this;
        }

        public Builder setMarginResource(@DimenRes int dimenResId) {
            mMarginResId = dimenResId;
            return this;
        }

        public Builder setMargins(int left, int top, int right, int bottom) {
            mMargins = new Rect(left, top, right, bottom);
            return this;
        }

        public ExtendedDividerItemDecoration build() {
            ExtendedDividerItemDecoration itemDecoration =
                    new ExtendedDividerItemDecoration(mContext, mIsVertical ? VERTICAL : HORIZONTAL);
            if (mDivider != null) {
                itemDecoration.setDrawable(mDivider);
            }
            itemDecoration.setTintList(mDrawableTintList);
            if (mDrawableTintMode != null) {
                itemDecoration.setTintMode(mDrawableTintMode);
            }
            if (mMargins != null) {
                itemDecoration.setMargins(mMargins.left, mMargins.top, mMargins.right, mMargins.bottom);
            } else {
                int marginSize = -1;
                if (mMarginSize != -1) {
                    marginSize = mMarginSize;
                } else if (mMarginResId != 0) {
                    marginSize = mContext.getResources().getDimensionPixelSize(mMarginResId);
                }
                if (marginSize != -1) {
                    if (mIsVertical) {
                        itemDecoration.setMargins(marginSize, 0, marginSize, 0);
                    } else {
                        itemDecoration.setMargins(0, marginSize, 0, marginSize);
                    }
                }
            }
            return itemDecoration;
        }
    }
}
