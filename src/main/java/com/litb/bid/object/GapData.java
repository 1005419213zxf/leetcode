package com.litb.bid.object;

public class GapData{
        private double gap;
        private double gapRatio;
        public GapData(double gap, double gapRatio) {
            super();
            this.gap = gap;
            this.gapRatio = gapRatio;
        }
        public double getGap() {
            return gap;
        }
        public void setGap(double gap) {
            this.gap = gap;
        }
        public double getGapRatio() {
            return gapRatio;
        }
        public void setGapRatio(double gapRatio) {
            this.gapRatio = gapRatio;
        }
    }