package com.ongl.chen.utils.spider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GXRFWJobDetail {
    String companyName;
    String postName;
    String salary;
    String exp;
    String benefit;
    String area;
    String publicTime;
    String href;
    String keyWord;
    String companyAptitude;
    String bref;
}
