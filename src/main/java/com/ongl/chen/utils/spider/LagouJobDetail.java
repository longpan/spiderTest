package com.ongl.chen.utils.spider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LagouJobDetail {
    String companyName;
    String postName;
    String salary;
    String exp;
    String industry;
    String benefit;
    String area;
    String publicTime;
    String href;

}
