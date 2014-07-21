package com.masterface.nxt.ae;

import java.util.List;

/**
 * Created by lyaf on 6/26/2014.
 */
public class LambdaExperiments {

    public static final int DAY = 24 * 60 * 60;

    public double[] getTradeVolumes(List<Trade> trades, int timeStamp) {
        TradeVolume tradeVolume = trades.stream().collect(
                () -> new TradeVolume(timeStamp),
                TradeVolume::accept,
                TradeVolume::combine);
        return tradeVolume.getVolume();
    }

    static class TradeVolume {

        private int timeStamp;
        private double[] volume = new double[4];

        TradeVolume(int timeStamp) {
            this.timeStamp = timeStamp;
        }

        public void accept(Trade trade) {
            long tradeTime = trade.getTimestamp();
            double tradeVolume = trade.getVolume();
            volume[3] += tradeVolume;
            if (!(tradeTime + 30 * DAY > timeStamp)) {
                return;
            }
            volume[2] += tradeVolume;
            if (!(tradeTime + 7 * DAY > timeStamp)) {
                return;
            }
            volume[1] += tradeVolume;
            if (!(tradeTime + DAY > timeStamp)) {
                return;
            }
            volume[0] += tradeVolume;
        }

        public void combine(TradeVolume tradeVolume) {
            volume[0] += tradeVolume.volume[0];
            volume[1] += tradeVolume.volume[1];
            volume[2] += tradeVolume.volume[2];
            volume[3] += tradeVolume.volume[3];
        }

        public double[] getVolume() {
            return volume;
        }
    }
}
