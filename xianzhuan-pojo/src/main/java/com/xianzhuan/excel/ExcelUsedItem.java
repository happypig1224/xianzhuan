package com.xianzhuan.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ContentRowHeight(20)
@HeadRowHeight(25)
public class ExcelUsedItem {
    @ExcelProperty("商品名称")
    @ColumnWidth(20)
    private String name;

    @ExcelProperty("商品分类")
    @ColumnWidth(15)
    private String categoryName;

    @ExcelProperty("原价")
    @ColumnWidth(15)
    private String originalPrice;

    @ExcelProperty("现价")
    @ColumnWidth(15)
    private String currentPrice;

    @ExcelProperty("图片")
    @ColumnWidth(30)
    private String image;

    @ExcelProperty("销量")
    @ColumnWidth(15)
    private String salesCount;

    @ExcelProperty("描述信息")
    @ColumnWidth(30)
    private String description;

    @ExcelProperty("规格")
    @ColumnWidth(30)
    private String specs;
}