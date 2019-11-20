
# 组织架构图

摘要：近期项目中需要实现组织架构图，搜索了大半天网上也没有体验良好、功能能够满足项目需要的demo，无奈只能自己写。实现思路是通过自定义view的方式，通过计算每一项的开始坐标来在canvas上定位，通过onTouchEvent监听手势的移动和缩放在进行画布的缩放和位移，实现项目需求。


## 需求

 1. **支持横竖屏切换**；
 2. **支持选中状态切换** ；
 3. item项 **字体颜色**、**背景颜色**、**选中颜色**、**字体大小**、**横向间距**、**纵向间距**(不考虑连线)自定义；
 4. 每**行的高度**取决于改行中的内容最大值，内容居中显示；
 5. 支持**手势**滑动、缩放；
 6. **缩放**比例最大值，最小值；

## 设计稿如下
![Alt](https://img-blog.csdnimg.cn/20191120141843173.png?x-oss-process=image/format,png#pic_center =270x480)


## 实现思路

首先数据源必须是树形结构，数据源只有一个跟结点，每个节点都可能有多个子节点，以此类推。

- 对数据源进行遍历，计算出最大行数和最大列数，目的是计算出所画布局的高drawHeight宽drawWidth
- 遍历解析出每个节点所在的行数和列数，方便计算节点的起始位置sX sY，这样就可以根据该行的行高直接画出给item项
- 第一种情况，如果显示内容的高宽都比view可视范围高宽小的话滑动是不考虑滑动和缩放
- 第二种情况，如果高宽其中有一项的值大于view可视范围的实际高宽，则计算出能全部看到范围的最小缩放比例，用于手动缩放时无限缩小的限制
- 左上角是原点（0,0），画的起点也是从原点开始，所以默认进来如果是上面第二种情况 则需要将画布canvas移动到中间位置，所以translateX translateY 至关重要
- 点击事件在onTouchEvent中实现，获取点击屏幕的x、y坐标遍历数据源跟所有item项所占区域坐标进行比较，判断是否是该项的选中，通过接口回调传递事件
## 实现步骤
## 属性定义

```java

<declare-styleable name="OrgMapView" >
        <!--绘制项的宽度-->
        <attr name="item_width" format="dimension|reference" />
        <!--item的高度-->
        <attr name="item_height" format="dimension|reference" />
        <!--item 横向间距-->
        <attr name="item_margin_h" format="dimension|reference" />
        <!--item 纵向间距-->
        <attr name="item_margin_v" format="dimension|reference" />
        <!--item 内容间距-纵向-->
        <attr name="item_padding_v" format="dimension|reference" />
        <attr name="item_line1_margin_top" format="dimension|reference" />
        <attr name="item_margin_to_line" format="dimension|reference" />
        <!--item 圆角大小-->
        <attr name="item_raidus" format="dimension|reference" />
        <!--item 默认背景色-->
        <attr name="item_bgcolor" format="color|reference" />
        <!--item 选中背景色-->
        <attr name="item_bgcolor_selected" format="color|reference" />
        <!--item 字体颜色-->
        <attr name="item_textcolor" format="color|reference" />
        <!--item 选中的背景颜色-->
        <attr name="item_textcolor_selected" format="color|reference" />
        <!--item 字体大小-->
        <attr name="item_textsize" format="dimension|reference" />
</declare-styleable>
```
## 数据源解析
采用递归的方式计算出总行数、总列数、所有叶子结点坐在的列数

```java
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
```
倒叙遍历该节点的父节点坐在的列数

```java
/*倒序遍历*/
        for (int i = totalLineCount; i > 0; i--) {
            paraseDataNodeRow(i, data);
        }
```

```java
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
```
计算每行最大的内容长度，用来确定行最高值

```java
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
```
实现效果如下：
![Alt](https://img-blog.csdnimg.cn/20191120175548700.gif)

存在问题：点击事件的时间间隔没有处理的很好，希望小伙伴能够给予好的意见和方案，欢迎评论转载
