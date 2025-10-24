package com.xianzhuan;

import com.xianzhuan.entity.ItemSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class XianZhuanMain {
    public static void main(String[] args) {
        List<ItemSpec> specs = new ArrayList<>();
        StringBuilder sb=new StringBuilder();
        specs.add(ItemSpec.builder().name("规格").value("全新").build());
        specs.add(ItemSpec.builder().name("规格").value("九成新").build());
        specs.add(ItemSpec.builder().name("规格").value("八成新").build());
        sb.append("规格:");
        for (int i = 0; i < specs.size(); i++) {
            if(i==0){
                sb.append(specs.get(i).getValue() + ",");
            }else{
                sb.append("," + specs.get(i).getValue());
            }
        }
        System.out.println("sb = " + sb);
    }
}
