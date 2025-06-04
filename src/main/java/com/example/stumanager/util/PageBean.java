package com.example.stumanager.util;

import lombok.Data;

import java.util.List;

@Data
public class PageBean<T> {
    private Integer pageno;  //第几页
    private Integer pagesize; //每页条数
    private List<T> datas;
    private Integer totalno; //总共几页
    private Integer totalsize;

    public PageBean(Integer pageno, Integer pagesize) {
        if(pageno<=0){
            this.pageno = 1;
        }else{
            this.pageno = pageno;
        }
        if(pagesize<=0){
            this.pagesize = 10;
        }else{
            this.pagesize = pagesize;
        }
        this.pageno = pageno;
        this.pagesize = pagesize;
    }

    /**
     * 开始索引
     */
    public Integer getStartIndex(){
        return (this.pageno-1)*this.pagesize;
    }


    @Override
    public String toString() {
        return "PageBean{" +
                "pageno=" + pageno +
                ", pagesize=" + pagesize +
                ", datas=" + datas +
                ", totalno=" + totalno +
                ", totalsize=" + totalsize +
                '}';
    }
}
