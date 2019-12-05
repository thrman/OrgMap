package com.coocpu.orgmap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 描述：组织架构图
 *
 * @author pc.
 * @Time 2019/11/20.
 */
public class OrgMapView extends View {

    /**
     * 屏幕状态(0:未点击,1:单手,否则多手指)
     */
    private int mMode;

    /**
     * 控件实际值
     */
    private float mWidth;
    private float mHeight;

    /**
     * 当前缩放范围
     */
    private float mZoom = 1f;

    /**
     * 最小缩放比例
     */
    private static final float S_ZOOM_MINI = 0.60000001f;
    /**
     * 最大缩放比例
     */
    private static final float S_ZOOM_MAX = 0.9999999f;

    /**
     * 记录左右两边的真实坐标点
     */
    private float mOriginX;
    private float mOriginY;
    /**
     * 记录新单指事件位移
     */
    private float mTagTranslateX;
    private float mTagTranslateY;

    /**
     * 画笔
     */
    private Paint mRectPaint;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    /*划线的画笔*/
    private Paint mPathPaint;
    private float oldDist;

    private final int ITEM_WIDTH_DEFAULT = 28;
    private final int ITEM_HEIGHT_DEFAULT = 157;
    private final int ITEM_MARGIN_H = 16;
    private final int ITEM_MARGIN_V = 10;
    private final int ITEM_RADIUS_DEFAULT = 16;
    private final int ITEM_BGCOLOR_DEFAULT = 0xffF6F6F6;
    private final int ITEM_BGCOLOR_SELECTED_DEFAULT = 0xff609961;
    private final int ITEM_TEXTCOLOR_DEFAULT = 0xff343434;
    private final int ITEM_TEXTCOLOR_SELECTED_DEFAULT = 0xffFFFFFF;
    private final int ITEM_TEXTSIXE_DEFAULT = 14;

    /*item宽度*/
    private int itemWidth;
    /*item横向间隔 即距离相邻item的横向距离*/
    private int itemMarginH;
    /*item纵向间隔 即距离相邻item的纵向距离*/
    private int itemMarginV;
    /*item 内容纵向间距*/
    private int itemPaddingV;
    /*第一行距离顶部的间隔*/
    private int itemLine1MarginTop;
    /*item距离线的间隔*/
    private int itemMarginToLine;
    /*item高度*/
    private int itemHeight;/*暂时不用， 由内容长度决定item显示高度*/
    /*item圆角*/
    private int itemRadius;
    /*item背景颜色*/
    private int itemBGColor;
    /*item背景颜色-选中*/
    private int itemBGColorSelected;
    /*item内容颜色*/
    private int itemTextColor;
    /*item内容选中颜色*/
    private int itemTextColorSelected;
    /*item字体大小*/
    private int itemTextSize;
    /*item背景画笔*/
    private Paint itemBgPaint;
    /*item内容画笔*/
    private Paint itemTextPaint;


    private boolean isClick;
    /**
     * 图层移动
     */
    private float mTranslationX;
    private float mTranslationY;
    /**
     * 最小缩放比例(就等于最佳缩放比例)
     */
    private float mZoomMini;

    /**
     * 绘制区域内容的宽和高
     */
    private float mRealHigh;
    private float mRealWidth;

    private float drawWidth;
    private float drawHeight;

    /**
     * 实际绘制的高宽
     */
    private float realDrawWidth;
    private float realDrawHeight;

    /**
     * 组织架构数据源
     */
    MorgDataBean data;
    /**
     * 解析数据源
     * 总列数-横向
     */
    private int totalRowCount = 0;
    /**
     * 解析数据源
     * 总行数-纵向
     */
    private int totalLineCount = 0;

    /**
     * 记录每层/行 内容最大长度  用于计算出该层item最大高度
     * 下标为0 表示第1行  以此类推
     */
    private int[] contentMaxLength4line;

    private float[] contentMaxHeigth4line;

    private float mOptTranslationX;
    private float mOptTranslationY;
    /**
     * 划线的路径
     */
    private Path path;

    ScreenTools screenTools;

    public OrgMapView(Context context) {
        super(context);
        init(context, null);
    }

    public OrgMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        screenTools = new ScreenTools(context);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(14);
        mTextPaint.setColor(0xffff0000);
        mTextPaint.setTextAlign(Paint.Align.CENTER);


        mCirclePaint = new Paint();
        mCirclePaint.setARGB(100, 255, 200, 80);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mRectPaint = new Paint();
        mRectPaint.setARGB(0xff, 0xff, 0xff, 0xff);
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.FILL);

        mPathPaint = new Paint();
        mPathPaint.setARGB(0xff, 0xD8, 0xD8, 0xD8);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(screenTools.dp2px(1));

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OrgMapView);

        itemWidth = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_width, ITEM_WIDTH_DEFAULT);
        itemHeight = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_height, ITEM_HEIGHT_DEFAULT);
        itemMarginH = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_margin_h, ITEM_MARGIN_H);
        itemMarginV = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_margin_v, ITEM_MARGIN_V);
        itemPaddingV = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_padding_v, 0);
        itemLine1MarginTop = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_line1_margin_top, 0);
        itemMarginToLine = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_margin_to_line, 0);
        itemRadius = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_raidus, ITEM_RADIUS_DEFAULT);
        itemBGColor = typedArray.getColor(R.styleable.OrgMapView_item_bgcolor, ITEM_BGCOLOR_DEFAULT);
        itemBGColorSelected = typedArray.getColor(R.styleable.OrgMapView_item_bgcolor_selected, ITEM_BGCOLOR_SELECTED_DEFAULT);
        itemTextColor = typedArray.getColor(R.styleable.OrgMapView_item_textcolor, ITEM_TEXTCOLOR_DEFAULT);
        itemTextColorSelected = typedArray.getColor(R.styleable.OrgMapView_item_textcolor_selected, ITEM_TEXTCOLOR_SELECTED_DEFAULT);
        itemTextSize = typedArray.getDimensionPixelSize(R.styleable.OrgMapView_item_textsize, ITEM_TEXTSIXE_DEFAULT);

        typedArray.recycle();


        itemBgPaint = new Paint();
        itemBgPaint.setColor(itemBGColor);
        itemBgPaint.setAntiAlias(true);

        itemTextPaint = new Paint();
        itemTextPaint.setColor(itemTextColor);
        itemTextPaint.setTextSize(itemTextSize);
        itemTextPaint.setAntiAlias(true);
    }

    /**
     * 设置数据源
     */
    public void setData(MorgDataBean data) {
        totalLineCount = 0;
        totalRowCount = 0;
        this.data = data;
        paraseDataLineAndLastNodeRow(this.data);
        if (totalLineCount > 0) {
            totalLineCount += 1;
        }
        /*倒序遍历*/
        for (int i = totalLineCount; i > 0; i--) {
            paraseDataNodeRow(i, data);
        }
        /*计算出每行最大字符数*/
        contentMaxLength4line = new int[totalLineCount];
        contentMaxHeigth4line = new float[totalLineCount];
        paraseDataContentMaxLength4Line(this.data);
        Log.e("----- 遍历之后的每行最大字符数=", Arrays.toString(contentMaxLength4line));
        drawWidth = totalRowCount * itemWidth + (totalRowCount + 1) * itemMarginH;


        float totalHeight = 0;
        for (float f : contentMaxHeigth4line) {
            totalHeight += f;
        }
        drawHeight = totalHeight + totalLineCount * itemMarginV + screenTools.dp2px(32);
        realDrawWidth = drawWidth;
        realDrawHeight = drawHeight;

        mRealWidth = Math.max(drawWidth, mWidth);
        mRealHigh = Math.max(drawHeight, mHeight);

        if (mWidth != 0 && mOptTranslationX == 0) {
            mOptTranslationY = mHeight / 2f - mRealHigh / 2f;
            mTranslationY = 0;
        }
        mOptTranslationX = mWidth / 2f - mRealWidth / 2f;
        mTranslationX = mOptTranslationX;
        /*随着横竖屏切换，重新计算最小缩放比例*/
        mZoomMini = Math.min(mWidth / mRealWidth, mHeight / mRealHigh);
        invalidate();
    }

    /**
     * 获取最原始的数据信息
     *
     * @return json data
     */
    public static String getOriginalFundData(Context context) {
        InputStream input = null;
        try {
            input = context.getAssets().open("val.json");
            String json = convertStreamToString(input);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * input 流转换为字符串
     *
     * @param
     * @return
     */
    private static String convertStreamToString(InputStream is) {
        String s = null;
        try {
            //格式转换
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) {
                s = scanner.next();
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = this.getMeasuredSize(widthMeasureSpec, true);
        int height = this.getMeasuredSize(heightMeasureSpec, false);

        setMeasuredDimension(width, height);
    }

    private int getMeasuredSize(int widthMeasureSpec, boolean b) {
        //模式
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        //尺寸
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        //计算所得的实际尺寸，要被返回
        int retSize = 0;
        //得到两侧的留边
        int padding = (b ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom());
        //对不同模式进行判断
        if (specMode == MeasureSpec.EXACTLY) {//显示指定控件大小
            retSize = specSize;
        } else {
            float totalHeight = 0;
            for (float f : contentMaxHeigth4line) {
                totalHeight += f;
            }
            retSize = b ? (totalRowCount + 1) * itemMarginH + totalRowCount * itemWidth : (int) (totalLineCount * itemMarginV + totalHeight + screenTools.dp2px(32));
            if (specMode == MeasureSpec.UNSPECIFIED) {
                retSize = Math.min(retSize, specSize);
            }
        }
        return retSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        mRealWidth = Math.max(drawWidth, mWidth);
        mRealHigh = Math.max(drawHeight, mHeight);

        if (mWidth != 0 && mOptTranslationX == 0) {
            mOptTranslationY = mHeight / 2f - mRealHigh / 2f;
            mTranslationY = 0;
        }
        mOptTranslationX = mWidth / 2f - mRealWidth / 2f;
        mTranslationX = mOptTranslationX;
        /*随着横竖屏切换，重新计算最小缩放比例*/
        mZoomMini = Math.min(mWidth / mRealWidth, mHeight / mRealHigh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mOriginX = ((mWidth / mZoom) / 2.0f - (mWidth / 2.0f - mTranslationX));
        mOriginY = mTranslationY;
        //移动缩放
        canvas.translate(mTranslationX, mTranslationY);
        canvas.scale(mZoom, mZoom, mWidth / 2f - mTranslationX, 0 - mTranslationY);
        Rect rect = new Rect();
        rect.left = 0;
        rect.top = 0;
        rect.right = (int) mRealWidth;
        rect.bottom = (int) mRealHigh;
        canvas.drawRect(rect, mRectPaint);

        if (data != null) {
            drawItems(canvas, data);
        }
    }

    /**
     * 递归画出 整体组织架构
     *
     * @param canvas
     * @param data
     */
    private void drawItems(Canvas canvas, MorgDataBean data) {
        if (data != null) {
            if (data.getCurrentLine() > 0) {
                /*画出 非root公司*/
                float sX = (data.getCurrentRow() - 1) * itemWidth + data.getCurrentRow() * itemMarginH /*+ itemMarginH*/;
                if (realDrawWidth < mRealWidth) {
                    sX += mRealWidth / 2.0f - realDrawWidth / 2.0f - mTranslationX;
                }
                float totalHeight = 0;
                for (int i = 0; i < data.getCurrentLine(); i++) {
                    totalHeight += contentMaxHeigth4line[i];
                }
                float sY = totalHeight + data.getCurrentLine() * itemMarginV + screenTools.dp2px(32);
                drawItem(canvas, sX, sY, data, data.isSelected());
            } else {
                drawRootItem(canvas, data);
            }
            if ((data.getChilds() == null || data.getChilds().size() == 0)) {
            } else {
                int i = 0;
                for (MorgDataBean d : data.getChilds()) {
                    drawItems(canvas, d);
                    if (i == 0) {
                        if (d.getCurrentLine() == 1) {
                            /*单独画出root item 的线*/
                            drawRootItemLine(canvas, data);
                        } else {
                            drawItemLine(canvas, d, data.getChilds().get(data.getChilds().size() - 1));
                        }
                    }
                    i++;
                }
            }
        }
    }

    private long touchDownTime = 0;
    /**
     * 上一次点击或者移动 的时刻毫秒数
     */
    private long clickorMovePreTime = 0;

    /**
     * 无论缩放与否 点击event.getX()/event.getY() 都是实际物理屏幕的X/Y的绝对值
     * eg:   缩小 点击点 右下角 getX=919.1489;event.getY=1632.0219
     * 放大 点击点 右下角 getX=922.1462;event.getY=1636.0198
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //第一次按下
            case MotionEvent.ACTION_DOWN:
                //记录只为单手移动算移动距离
                touchDownTime = System.currentTimeMillis();
                mMode = 1;
                isClick = true;
                mTagTranslateX = (event.getX() - mTranslationX * mZoom);
                mTagTranslateY = (event.getY() - mTranslationY * mZoom);
                break;
            //最后一个离开
            case MotionEvent.ACTION_UP:
                mMode = 0;
                mTagTranslateX = 0f;
                mTagTranslateY = 0f;
                if (isClick) {
                    //单击事件处理
                    float x = mWidth / mZoom - mOriginX - (mWidth - event.getX()) / mZoom /*- itemMarginH / mZoom */;
                    if (drawWidth < mRealWidth) {
                        x = x + mTranslationX / 2.0f;
                    }
                    float y = event.getY() / mZoom - mTranslationY;
                    /*确保是点击才试做点击动作， 滑动后抬起则不算是点击*/
                    if (System.currentTimeMillis() - touchDownTime < 300) {
                        if (System.currentTimeMillis() - clickorMovePreTime > 1000) {
                            clickorMovePreTime = System.currentTimeMillis();
                            paraseSelectedItem(data, x, y);
                            invalidate();
                        }
                    }
                }
                break;
            //非最后一个离开
            case MotionEvent.ACTION_POINTER_UP:
                isClick = false;
                mMode -= 1;
                mTagTranslateX = 0f;
                mTagTranslateY = 0f;
                break;
            //非第一次按下
            case MotionEvent.ACTION_POINTER_DOWN:
                isClick = false;
                oldDist = spacing(event);
                mMode += 1;
                mTagTranslateX = 0f;
                mTagTranslateY = 0f;
                break;
            //移动
            case MotionEvent.ACTION_MOVE:
                if (realDrawWidth < mRealWidth && realDrawHeight < mRealHigh) {
                    break;
                }
                if (mMode >= 2) {
                    isClick = false;
                    //两个手指以上 两点间的距离
                    float newDist = spacing(event);
                    if (newDist > oldDist + 1) {
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                    if (newDist < oldDist - 1) {
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                } else if (mMode == 1) {//只有一根手指
                    /*第一次第一个触点 按下*/
                    if (mZoom <= mZoomMini) {
                        invalidate();
                        break;
                    }
                    if (mTagTranslateX != 0) {
                        /**
                         * 保证临界边 在可视屏幕内
                         */
                        if (realDrawWidth * mZoom > mWidth) {
                            mTranslationX = (event.getX() - mTagTranslateX) / mZoom;
                            mOriginX = ((mWidth / mZoom) / 2f - (mWidth / 2f - mTranslationX));
                            if (mOriginX > 0) {//向右滑动 左边临界超越界限进入可视屏幕内
                                if (mZoom > mZoomMini) {/*非最小情况*/
                                    mTranslationX -= mOriginX;
                                    mOriginX = 0;
                                    mTagTranslateX = (event.getX() - mTranslationX * mZoom);
                                } else {

                                }
                            } else if ((mOriginX + mRealWidth) < mWidth / mZoom) {//向左滑动 右边临界超越界限进入可视屏幕内
                                if (realDrawWidth * mZoom < mWidth) {
                                    break;
                                }
                                if (mZoom > mZoomMini) {/*非最小情况*/
                                    mTranslationX += mWidth / mZoom - mOriginX - mRealWidth;
                                    mOriginX = -mRealWidth;
                                    mTagTranslateX = (event.getX() - mTranslationX * mZoom);
                                }
                            }
                        }
                        if (realDrawHeight * mZoom > mHeight) {
                            mTranslationY = (event.getY() - mTagTranslateY) / mZoom;
                            mOriginY = mTranslationY;
                            if (mOriginY > 0) {//向下滑动 上边临界超越界限进入可视屏幕内
                                if (realDrawHeight * mZoom < mHeight) {
                                    break;
                                }
                                if (mZoom > mZoomMini) {/*非最小情况*/
                                    mTranslationY -= mOriginY;
                                    mOriginY = 0;
                                    mTagTranslateY = (event.getY() - mTranslationY * mZoom);
                                }
                            } else if ((mOriginY + mRealHigh) < mHeight / mZoom) {//向上滑动 底边临界超越界限进入可视屏幕内

                                if (mZoom > mZoomMini) {/*非最小情况*/
                                    mTranslationY += mHeight / mZoom - mOriginY - mRealHigh;
                                    mOriginY = -mRealHigh;
                                    mTagTranslateY = (event.getY() - mTranslationY * mZoom);
                                }
                            }
                        }
                        invalidate();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * @param event 计算两点距离
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * @param v 转化缩放等级
     */
    private void zoom(float v) {
        float historyZoom = mZoom;
        mZoom *= v;
        if (mZoom <= mZoomMini) {
            mTranslationX = mOptTranslationX;
            mTranslationY = 0;
            mTagTranslateX = 0f;
            mTagTranslateY = 0f;
            mZoom = mZoomMini;
            invalidate();
            return;
        } else if (mZoom >= S_ZOOM_MAX) {
            mZoom = S_ZOOM_MAX;
            invalidate();
            return;
        }
        if (historyZoom > mZoom) {
            mTranslationX = mTranslationX - ((mTranslationX - mOptTranslationX) / (mZoom - mZoomMini)) * (historyZoom - mZoom);
            mOptTranslationY = 0;
            mTranslationY = 0;
        }
        invalidate();
    }

    /**
     * 画组织结构单项item
     *
     * @param canvas     画布
     * @param sX         开始x坐标
     * @param sY         开始y坐标
     * @param data       文本内容
     * @param isSelected 是否是选中状态
     */
    private void drawItem(Canvas canvas, float sX, float sY, MorgDataBean data, boolean isSelected) {
        if (TextUtils.isEmpty(data.getOrgname()) || data.getOrgname().trim().length() == 0) {
            return;
        }
        itemBgPaint.setColor(isSelected ? itemBGColorSelected : itemBGColor);
        itemTextPaint.setColor(isSelected ? itemTextColorSelected : itemTextColor);
        itemTextPaint.setTextSize(itemTextSize);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        itemTextPaint.setTypeface(font);

        /*画item背景*/
        canvas.drawRoundRect(sX, sY, sX + itemWidth, sY + contentMaxHeigth4line[data.getCurrentLine()], itemRadius, itemRadius, itemBgPaint);
        /*
         * 相对位置中，基线是0，即绘制文字的y轴起点是从基线位置开始的。 top是负数 bottom是证书
         * top 负数，基线上方,绘制文字占用空间的顶部
         * bottom 正数，基线下方,绘制文字占用空间的底部
         * ascent 负数,基线上方，绘制文字顶部(非绘制占用空间的顶部)
         * descent 正数，基线下方，绘制文字底部(非绘制占用空间的底部)
         * */
        Paint.FontMetrics fontMetrics = itemTextPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        Rect r = new Rect();
        int contentLength = data.getOrgname().length();
        /*绘制内容总高度*/
        int totalContentHeight = 0;
        for (int i = 0; i < contentLength; i++) {
            totalContentHeight += bottom - top;
        }
        /*内容绘制的开始y坐标*/
        float indexSY = sY + (contentMaxHeigth4line[data.getCurrentLine()] - totalContentHeight) / 2.0f;
        for (int i = 0; i < contentLength; i++) {
            itemTextPaint.getTextBounds(String.valueOf(data.getOrgname().charAt(i)), 0, 1, r);
            canvas.drawText(String.valueOf(data.getOrgname().charAt(i)), sX + (itemWidth - r.width()) / 2.0f, indexSY + Math.abs(fontMetrics.top), itemTextPaint);
            indexSY += bottom - top;
        }
    }

    /**
     * 画出根item
     *
     * @param canvas 画布
     * @param data   根数据
     */
    private void drawRootItem(Canvas canvas, MorgDataBean data) {
        float sX = 0;
        float sY = 0;
        itemTextPaint.setColor(0xff1d1d1d);
        itemTextPaint.setTextSize(screenTools.sp2px(getContext(), 18));
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        itemTextPaint.setTypeface(font);
        Rect r = new Rect();
        itemTextPaint.getTextBounds(data.getOrgname(), 0, data.getOrgname().length(), r);
        sY = screenTools.dp2px(32);
        if (realDrawWidth < mRealWidth) {
            sX = mRealWidth / 2.0f - r.width() / 2.0f - mTranslationX;
        } else {
            sX = mRealWidth / 2.0f - r.width() / 2.0f;
        }

        canvas.drawText(data.getOrgname(), sX, sY, itemTextPaint);
    }

    /**
     * 画出跟item 的连线
     *
     * @param canvas
     * @param data
     */
    private void drawRootItemLine(Canvas canvas, MorgDataBean data) {
        if (data == null || data.getChilds() == null || data.getChilds().size() == 0) {
            return;
        }
        if (path == null) {
            path = new Path();
        }
        path.reset();
        float sX = (data.getChilds().get(0).getCurrentRow() - 1) * itemWidth + data.getChilds().get(0).getCurrentRow() * itemMarginH/* + itemMarginH */ + itemWidth / 2.0f;

        float totalHeight = 0;
        for (int i = 0; i < data.getChilds().get(0).getCurrentLine(); i++) {
            totalHeight += contentMaxHeigth4line[i];
        }
        float sY = totalHeight + data.getChilds().get(0).getCurrentLine() * itemMarginV + screenTools.dp2px(32) - itemMarginToLine;
        float eX = (data.getChilds().get(data.getChilds().size() - 1).getCurrentRow() - 1) * itemWidth + data.getChilds().get(data.getChilds().size() - 1).getCurrentRow() * itemMarginH /*+ itemMarginH*/ + itemWidth / 2.0f;

        if (realDrawWidth < mRealWidth) {
            sX += mRealWidth / 2.0f - realDrawWidth / 2.0f;
            eX += mRealWidth / 2.0f - realDrawWidth / 2.0f;
        }

        path.moveTo(sX, sY);
        path.lineTo(sX, sY - (itemMarginV - 2 * itemMarginToLine) / 2.0f);
        path.lineTo(eX, sY - (itemMarginV - 2 * itemMarginToLine) / 2.0f);
        path.lineTo(eX, sY);

        float sLineSX = 0;
        if (drawWidth < mRealWidth) {
            sLineSX = mRealWidth / 2.0f - mTranslationX;
        } else {
            sLineSX = mRealWidth / 2.0f;
        }
        path.moveTo(sLineSX, sY - (itemMarginV - 2 * itemMarginToLine) / 2.0f);
        path.lineTo(sLineSX, sY - (itemMarginV - 2 * itemMarginToLine));
        canvas.drawPath(path, mPathPaint);
    }

    /**
     * 画两个点之间的连线
     *
     * @param start
     * @param end
     */
    private void drawItemLine(Canvas canvas, MorgDataBean start, MorgDataBean end) {
        if (path == null) {
            path = new Path();
        }
        path.reset();
        float sX = (start.getCurrentRow() - 1) * itemWidth + start.getCurrentRow() * itemMarginH /*+ itemMarginH*/ + itemWidth / 2.0f;

        if (realDrawWidth < mRealWidth) {
            sX += mRealWidth / 2.0f - realDrawWidth / 2.0f - mTranslationX;
        }

        float totalHeight = 0;
        for (int i = 0; i < start.getCurrentLine(); i++) {
            totalHeight += contentMaxHeigth4line[i];
        }
        float sY = totalHeight + start.getCurrentLine() * itemMarginV + screenTools.dp2px(32) - itemMarginToLine;
        float eX = (end.getCurrentRow() - 1) * itemWidth + end.getCurrentRow() * itemMarginH /*+ itemMarginH*/ + itemWidth / 2.0f;
        path.moveTo(sX, sY);
        path.lineTo(sX, sY - (itemMarginV - 2 * itemMarginToLine) / 2.0f);
        path.lineTo(eX, sY - (itemMarginV - 2 * itemMarginToLine) / 2.0f);
        path.lineTo(eX, sY);
        path.moveTo((sX + eX) / 2.0f, sY - (itemMarginV - 2 * itemMarginToLine) / 2.0f);
        path.lineTo((sX + eX) / 2.0f, sY - (itemMarginV - 2 * itemMarginToLine));
        canvas.drawPath(path, mPathPaint);
    }

    /**
     * 递归解析数据
     * a,计算出总列数和总行数
     * b,所有叶子结点项设置所在列的值
     *
     * @param data 数据源
     */
    private void paraseDataLineAndLastNodeRow(MorgDataBean data) {
        if (data == null || data.getChilds() == null || data.getChilds().size() == 0) {
            return;
        }
        for (MorgDataBean d : data.getChilds()) {
            /*自己所处的行数=父节点所在的行数+1
             从上到下的顺序*/
            d.setCurrentLine(data.getCurrentLine() + 1);
            /*总行数也是根据行数的增加而增加的*/
            if (data.getCurrentLine() + 1 > totalLineCount) {
                totalLineCount = data.getCurrentLine() + 1;
            }
            /*所有分支的叶子结点数 即该叶子结点项所在的列数*/
            if (d.getChilds() == null || d.getChilds().size() == 0) {
                /*列数+1*/
                totalRowCount++;
                d.setCurrentRow(totalRowCount);
            }
            paraseDataLineAndLastNodeRow(d);
        }
    }

    /**
     * 目的修改 该级父对象即data 的所在列数
     *
     * @param level 遍历的级数 需要倒数遍历 从大到小的顺序
     * @param data  数据源
     */
    private void paraseDataNodeRow(int level, MorgDataBean data) {
        if (data == null) {
            return;
        }

        if (data.getChilds() == null) {
            return;
        }
        for (MorgDataBean d : data.getChilds()) {
            if (d.getCurrentLine() == level && data.getCurrentRow() == 0) {
                data.setCurrentRow((d.getCurrentRow() + data.getChilds().get(data.getChilds().size() - 1).getCurrentRow()) / 2.0f);
            } else {
                paraseDataNodeRow(level, d);
            }
        }
    }

    /**
     * 计算每行line 的最大内容长度
     */
    private void paraseDataContentMaxLength4Line(MorgDataBean data) {
        if (data == null) {
            return;
        }
        for (MorgDataBean d : data.getChilds()) {
            if (!isEmpty(d.getOrgname()) && contentMaxLength4line.length > d.getCurrentLine()) {
                contentMaxLength4line[d.getCurrentLine()] = Math.max(contentMaxLength4line[d.getCurrentLine()], d.getOrgname().trim().length());
                contentMaxHeigth4line[d.getCurrentLine()] = getTotalContentHeight(contentMaxLength4line[d.getCurrentLine()]);
            }
            if (d.getChilds() != null && d.getChilds().size() > 0) {
                paraseDataContentMaxLength4Line(d);
            }
        }
    }

    /**
     * 获得 输入字符的显示占用的高度(字符本身高度+内容纵向间距*2)
     *
     * @param contentLength
     * @return
     */
    private float getTotalContentHeight(int contentLength) {
        if (contentLength == 0) {
            return 0;
        }
        /*
         * 相对位置中，基线是0，即绘制文字的y轴起点是从基线位置开始的。 top是负数 bottom是证书
         * top 负数，基线上方,绘制文字占用空间的顶部
         * bottom 正数，基线下方,绘制文字占用空间的底部
         * ascent 负数,基线上方，绘制文字顶部(非绘制占用空间的顶部)
         * descent 正数，基线下方，绘制文字底部(非绘制占用空间的底部)
         * */
        itemTextPaint.setTextSize(itemTextSize);
        Paint.FontMetrics fontMetrics = itemTextPaint.getFontMetrics();
        /*绘制内容总高度*/
        return (fontMetrics.bottom - fontMetrics.top) * contentLength + itemPaddingV * 2;

    }

    /**
     * 找出点击的item 标记为 选中状态
     *
     * @param data 数据源
     * @param x    点击的x坐标
     * @param y    点击的y坐标
     */
    private void paraseSelectedItem(MorgDataBean data, float x, float y) {
        if (data != null) {
            float sX = (data.getCurrentRow() - 1) * itemWidth + data.getCurrentRow() * itemMarginH;
            float totalHeight = 0;
            for (int i = 0; i < data.getCurrentLine(); i++) {
                totalHeight += contentMaxHeigth4line[i];
            }
            float sY = totalHeight + data.getCurrentLine() * itemMarginV + screenTools.dp2px(32);
            if (realDrawWidth < mRealWidth) {
                sX += mRealWidth / 2.0f - realDrawWidth / 2.0f;
            }
            if ((x > sX && x < sX + itemWidth && y > sY && y < sY + contentMaxHeigth4line[data.getCurrentLine()])) {
                /*点击点 为非空白区域， 重置所有数据为非选中状态， 然后标记当前项为选中状态*/
                /*如果有选中 则将原数据重置为非选中*/
                paraseResetItems(this.data);
                /*将该项置为选中状态 刷新动作放在调用函数*/
                data.setSelected(true);
                if (orgMapItemClickListener != null) {
                    orgMapItemClickListener.onItemClick(data);
                }
                return;
            }
            if (data.getChilds() != null) {
                for (MorgDataBean d : data.getChilds()) {
                    paraseSelectedItem(d, x, y);
                }
            }
        }
    }

    /**
     * 重置所有数据为非选中状态
     *
     * @param data
     */
    private void paraseResetItems(MorgDataBean data) {
        if (data != null) {
            data.setSelected(false);
            if (data.getChilds() != null) {
                for (MorgDataBean d : data.getChilds()) {
                    paraseResetItems(d);
                }
            }
        }
    }

    private OnOrgMapItemClickListener orgMapItemClickListener;

    public void setOrgMapItemClickListener(OnOrgMapItemClickListener orgMapItemClickListener) {
        this.orgMapItemClickListener = orgMapItemClickListener;
    }

    public interface OnOrgMapItemClickListener {
        void onItemClick(MorgDataBean data);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
}
