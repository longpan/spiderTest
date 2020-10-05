package com.ongl.chen.utils.spider;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GXRFWJobDetail {
    @ExcelProperty("公司名称")
    String companyName;
    @ExcelProperty("岗位名称")
    String postName;
    @ExcelProperty("薪资")
    String salary;
    @ExcelProperty("经验要求")
    String exp;
    @ExcelProperty("福利")
    String benefit;
    @ExcelProperty("上班地点")
    String area;
    @ExcelProperty("发布时间")
    String publicTime;
    @ExcelProperty("链接")
    String href;
    @ExcelProperty("关键字")
    String keyWord;
    @ExcelProperty("公司资质")
    String companyAptitude;
    @ExcelProperty("简介")
    String bref;
}
