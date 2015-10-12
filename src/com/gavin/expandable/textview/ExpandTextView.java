package com.gavin.expandable.textview;

import java.io.ObjectInputValidation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 可伸缩的TextView
 * <p>
 * xml文件中使用该自定义View Demo:
 * <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    xmlns:expand="http://schemas.android.com/apk/res/com.gavin.expandable.textview"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    
    tools:context="com.gavin.expandable.textview.MainActivity" >

    <com.gavin.expandable.textview.ExpandTextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        expand:text="@string/content_one"
        expand:textcolor="#FF0000"
        expand:textsize="15sp"
        expand:icon="@drawable/text_expand"
        expand:lines="2"
        />
 * </LinearLayout>
 * </p>
 * <p>
 *  text:设置ExpandTextView的文本内容
 *  textcolor:设置文本的颜色
 *  textsize:设置文本字体大小
 *  icon:设置展开和压缩的图标
 *  lines:设置压缩时(非展开),显示多少行
 * </p>
 * 
 * com.gavin.expandable.textview.ExpandTextView
 * @author yuanzeyao <br/>
 * create at 2015年10月10日 下午5:41:12
 */
public class ExpandTextView extends LinearLayout implements OnClickListener{
  public static final String TAG="ExpandTextView";
  
  public static final int DEFAULT_TEXT_COLOR=0XFF000000;
  public static final int DEFAULT_LINE_NUM=3;
  public static final int DEFAULT_TEXT_SIZE=12;
  public static final int DEFAULT_MARGIN_TOP=10;
  private TextView mTextView;
  private ImageView mImageView;
  /**TextView字体的颜色*/
  private int textColor;
  /**TextView字体的大小*/
  private float textSize;
  /**TextView默认显示行数*/
  private int maxLine;
  /**TextView的文本内容*/
  private String text;
  /**ImageView使用的图片*/
  private Drawable mIcon;
  /**TextView所有的内容暂用的行数*/
  private int contentLine=0;
  /**是否展开*/
  private boolean isExpand=false;
  
  public ExpandTextView(Context context) {
    super(context);
    init(null,0);
  }
  
  public ExpandTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs,0);
  }
  
  public ExpandTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs,defStyleAttr);
  }
  
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ExpandTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs,defStyleAttr);
  }
  
  private void init(AttributeSet attrs,int defStyleAttr){
    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER_HORIZONTAL);
    TypedArray array=this.getContext().obtainStyledAttributes(attrs, R.styleable.ExpandTextView,defStyleAttr,0);
    textColor=array.getColor(R.styleable.ExpandTextView_textcolor,DEFAULT_TEXT_COLOR);
    textSize=array.getDimensionPixelOffset(R.styleable.ExpandTextView_textsize,dp2px(DEFAULT_TEXT_SIZE));
    maxLine=array.getInt(R.styleable.ExpandTextView_lines,DEFAULT_LINE_NUM);
    mIcon=array.getDrawable(R.styleable.ExpandTextView_icon);
    text=array.getString(R.styleable.ExpandTextView_text);
    if(mIcon==null){
      mIcon=this.getContext().getResources().getDrawable(R.drawable.text_expand);
    }
    array.recycle();
    initViewAttribute();
  }
  
  private void initViewAttribute(){
    mTextView=new TextView(this.getContext());
    //设置属性
    mTextView.setText(text);
    mTextView.setTextColor(textColor);
    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

    
    int textHeight=mTextView.getLineHeight()*maxLine;
    LayoutParams mParams_txt=new LayoutParams(LayoutParams.MATCH_PARENT,textHeight);
    addView(mTextView,mParams_txt);
    
    mImageView=new ImageView(this.getContext());
    mImageView.setImageDrawable(mIcon);
    LayoutParams mParams_img=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
    mParams_img.topMargin=dp2px(DEFAULT_MARGIN_TOP);
    addView(mImageView,mParams_img);
    mImageView.setOnClickListener(this);
    this.setOnClickListener(this);
    
    this.post(new Runnable() {
      @Override
      public void run() {
        contentLine=mTextView.getLineCount();
        if(contentLine<=maxLine){
          mImageView.setVisibility(View.GONE);
          LayoutParams mParam=(LayoutParams) mTextView.getLayoutParams();
          mParam.height=LayoutParams.WRAP_CONTENT;
          mTextView.setLayoutParams(mParam);
          ExpandTextView.this.setOnClickListener(null);
        }else{
          //默认是非展开模式，那么设置最大行为maxLine
          mTextView.setMaxLines(maxLine);
          mTextView.setEllipsize(TruncateAt.END);
          mImageView.setVisibility(View.VISIBLE);
        }
      }
    });
  }
  
  /**
   * dp单位和px单位的转化
   * @param dp
   * @return
   */
  private int dp2px(int dp){
    return (int)(this.getResources().getDisplayMetrics().density*dp+0.5);
  }

  @Override
  public void onClick(View v) {
    if(v==mImageView|| v==this){
      flexibleHeight();
    }
  }

  /**
   * 对TextView进行伸缩处理
   */
  private void flexibleHeight() {
    isExpand=!isExpand;
    int textHeight=0;
    float startDegree=0.0f;
    float endDegree=180.0f;
    if(isExpand){
      //如果是展开模式，那么取消最大行为maxLine的限制
      textHeight=contentLine*mTextView.getLineHeight();
      mTextView.setMaxLines(contentLine);
    }else{
      textHeight=mTextView.getLineHeight()*maxLine;
      endDegree=0.0f;
      startDegree=180.0f;
    }
    final LayoutParams mParam=(LayoutParams) mTextView.getLayoutParams();
    //TextView的平移动画
    ValueAnimator animator_textView= ValueAnimator.ofInt(mTextView.getHeight(),textHeight);
    animator_textView.addUpdateListener(new AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mParam.height=(Integer)animation.getAnimatedValue();
        mTextView.setLayoutParams(mParam);
      }
    });
    //imageView的旋转动画
    ObjectAnimator animator_img=ObjectAnimator.ofFloat(mImageView, "rotation", startDegree, endDegree);
    
    AnimatorSet mAnimatorSets=new AnimatorSet();
    mAnimatorSets.setDuration(500);
    mAnimatorSets.play(animator_img).with(animator_textView);
    mAnimatorSets.start();
    mAnimatorSets.addListener(new AnimatorListenerAdapter(){
      @Override
      public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        //动画结束之后，如果是非展开模式，则设置最大行数为maxLine
        if(!isExpand){
          mTextView.setMaxLines(maxLine);
        }
      }
      
    });
  }
}
