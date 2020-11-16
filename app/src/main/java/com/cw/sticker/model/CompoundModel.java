package com.cw.sticker.model;

import java.util.List;

public class CompoundModel {

    /**
     * type : 2
     * bgTexture : background-3
     * sku : {"skuName":"华为 p30","skuCode":"CA00514152GS"}
     * items : [{"texture":"material-2-12","tint":0,"fillType":1,"type":1,"position":{"x":78.75067175292983,"y":-59.249318074544384},"scale":3.6186744028473403,"rotation":2.2989704608917236,"anchor":{"x":0.5,"y":0.5}},{"type":3,"tint":0,"text":"犹犹豫豫","position":{"x":100.25066666666666,"y":243.25066666666666},"rotation":4.738763809204102,"scale":3.6,"fillType":1,"anchor":{"x":0.5,"y":0.5}}]
     */

    public List<ItemsBean> items;

    public static class ItemsBean {
        /**
         * texture : material-2-12
         * tint : 0
         * fillType : 1
         * type : 1
         * position : {"x":78.75067175292983,"y":-59.249318074544384}
         * scale : 3.6186744028473403
         * rotation : 2.2989704608917236
         * anchor : {"x":0.5,"y":0.5}
         * text : 犹犹豫豫
         */

        public int tint;
        public int fillType;
        public int type;
        public double scale;
        public double rotation;
        public PositionBean position;
        public AnchorBean anchor;

        public String text;
        public String texture;

        public static class PositionBean {
            /**
             * x : 78.75067175292983
             * y : -59.249318074544384
             */

            public float x;
            public float y;

            public PositionBean(float x, float y) {
                this.x = x;
                this.y = y;
            }
        }

        public static class AnchorBean {
            /**
             * x : 0.5
             * y : 0.5
             */

            public float x;
            public float y;

            public AnchorBean(float x, float y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
