package com.sunny.myviews.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sunny.myviews.R;

import java.util.ArrayList;

/**
 * Created by slp on 2018/4/26.
 */
public class ChatEditText extends AppCompatEditText {

    private int itemPadding;
    private StringBuilder mBuilder;

    private OnMentionInputListener mOnMentionInputListener;

    public ChatEditText(Context context) {
        super(context);
        init();
    }


    public ChatEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        itemPadding = dip2px(getContext(), 1);
        addTextChangedListener(new MentionTextWatcher());
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        MyImageSpan[] spans = getText().getSpans(0, getText().length(), MyImageSpan.class);
        for (MyImageSpan myImageSpan : spans) {
            if (getText().getSpanEnd(myImageSpan) - 1 == selStart) {
                selStart = selStart + 1;
                setSelection(selStart);
                break;
            }
        }
        super.onSelectionChanged(selStart, selEnd);
    }

//    private void flushSpans() {
//        Editable editText = getText();
//        Spannable spannableString = new SpannableString(editText);
//        MyImageSpan[] spans = spannableString.getSpans(0, editText.length(), MyImageSpan.class);
//        List<UnSpanText> texts = getAllTexts(spans, editText);
//        for (UnSpanText unSpanText : texts) {
//            if (!TextUtils.isEmpty(unSpanText.showText.toString().trim())) {
//                generateOneSpan(spannableString, unSpanText);
//            }
//        }
//        setText(spannableString);
//        setSelection(spannableString.length());
//    }

//    private List<UnSpanText> getAllTexts(MyImageSpan[] spans, Editable edittext) {
//        List<UnSpanText> texts = new ArrayList<>();
//        int start;
//        int end;
//        CharSequence text;
//        List<Integer> sortStartEnds = new ArrayList<>();
//        sortStartEnds.add(0);
//        for (MyImageSpan myImageSpan : spans) {
//            sortStartEnds.add(edittext.getSpanStart(myImageSpan));
//            sortStartEnds.add(edittext.getSpanEnd(myImageSpan));
//        }
//        sortStartEnds.add(edittext.length());
//        Collections.sort(sortStartEnds);
//        for (int i = 0; i < sortStartEnds.size(); i = i + 2) {
//            start = sortStartEnds.get(i);
//            end = sortStartEnds.get(i + 1);
//            text = edittext.subSequence(start, end);
//            if (!TextUtils.isEmpty(text)) {
//                texts.add(new UnSpanText(start, end, text));
//            }
//        }
//
//        return texts;
//    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
//            flushSpans();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    //添加一个Span
    public void addSpan(String showText, int userId) {
        mBuilder = new StringBuilder();
        mBuilder.append(showText);
        getText().insert(getSelectionStart(), mBuilder.toString());
//        getText().append(showText);
        SpannableString spannableString = new SpannableString(getText());
        generateOneSpan(userId,spannableString, new UnSpanText( getSelectionEnd() - mBuilder.toString().length()-1, getSelectionEnd(), "@"+mBuilder.toString()));
        setText(spannableString);
        setSelection(spannableString.length());
    }

    /**
     * 获取@的用户id List
     * @return
     */
    public ArrayList<Integer> getUserIdList(){
        ArrayList<Integer> userIdList = new ArrayList<>();
        MyImageSpan[] spans = getText().getSpans(0, getText().length(), MyImageSpan.class);
        for(MyImageSpan myImageSpan:spans){
            userIdList.add(myImageSpan.getUserId());
        }
        return userIdList;
    }

    private void generateOneSpan(int userId, Spannable spannableString, UnSpanText unSpanText) {
        View spanView = getSpanView(getContext(), unSpanText.showText.toString(), getMaxWidth());
        BitmapDrawable bitmapDrawable = (BitmapDrawable) convertViewToDrawable(spanView);
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());
        MyImageSpan what = new MyImageSpan(bitmapDrawable, unSpanText.showText.toString(), userId);
        final int start = unSpanText.start;
        final int end = unSpanText.end;
        spannableString.setSpan(what, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public Drawable convertViewToDrawable(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        cacheBmp.recycle();
        view.destroyDrawingCache();
        return new BitmapDrawable(viewBmp);
    }

    public View getSpanView(Context context, String text, int maxWidth) {
        TextView view = new TextView(context);
        view.setMaxWidth(maxWidth);
        view.setText(text);
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setSingleLine(true);
        view.setBackgroundResource(R.drawable.shape_corner_rectangle);
        view.setTextSize(getTextSize());
        view.setTextColor(getCurrentTextColor());
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setPadding(itemPadding, itemPadding, itemPadding, itemPadding);
        frameLayout.addView(view);
        return frameLayout;
    }

    private class UnSpanText {
        int start;
        int end;
        CharSequence showText;

        UnSpanText(int start, int end, CharSequence showText) {
            this.start = start;
            this.end = end;
            this.showText = showText;
        }
    }

    private class MyImageSpan extends ImageSpan {
        private String showText;
        private int userId;

        public MyImageSpan(Drawable d, String showText, int userId) {
            super(d);
            this.showText = showText;
            this.userId = userId;
        }

        public String getShowText() {
            return showText;
        }

        public int getUserId() {
            return userId;
        }
    }

    /**
     * dip转换px
     */
    public static int dip2px(Context context, int dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    //text watcher for mention character('@')
    private class MentionTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count == 1 && s.charAt(s.length() - 1) == "@".charAt(0)) {
                if(mOnMentionInputListener != null){
                    mOnMentionInputListener.onMentionCharacterInput();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * set listener for mention character('@')
     *
     * @param onMentionInputListener MentionEditText.OnMentionInputListener
     */
    public void setOnMentionInputListener(OnMentionInputListener onMentionInputListener) {
        mOnMentionInputListener = onMentionInputListener;
    }

    public interface OnMentionInputListener {
        /**
         * call when '@' character is inserted into EditText
         */
        void onMentionCharacterInput();
    }
}
