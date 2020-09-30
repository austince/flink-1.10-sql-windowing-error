package org.example;

import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * Trigger the combines time and count based windowing, useful in batching situations where a window
 * may either never fill up or fill up slowly.
 *
 * <p>This trigger is based on a combination of Flink's
 * {@link org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger} and
 * {@link org.apache.flink.streaming.api.windowing.triggers.CountTrigger} triggers.
 */
public class TimedCountTrigger extends Trigger<Object, TimeWindow> {
  private static final long serialVersionUID = 1L;

  protected final long maxCount;

  private final ReducingStateDescriptor<Long> stateDesc =
      new ReducingStateDescriptor<>("count", Long::sum, LongSerializer.INSTANCE);

  protected TimedCountTrigger(long maxCount) {
    this.maxCount = maxCount;
  }

  public static TimedCountTrigger of(long maxCount) {
    return new TimedCountTrigger(maxCount);
  }

  public TriggerResult onElement(Object element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {

    ctx.registerProcessingTimeTimer(window.maxTimestamp());

    ReducingState<Long> count = ctx.getPartitionedState(this.stateDesc);
    count.add(1L);

    if (count.get() >= this.maxCount) {
      // clear the count and also clear the timer - if another one comes in it'll register a timer onElement
      count.clear();
      ctx.deleteProcessingTimeTimer(window.maxTimestamp());
      return TriggerResult.FIRE_AND_PURGE;
    } else {
      return TriggerResult.CONTINUE;
    }
  }


  public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) {
    return TriggerResult.CONTINUE;
  }


  public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) {
    return TriggerResult.FIRE;
  }


  public void clear(TimeWindow window, TriggerContext ctx) {
    ctx.deleteProcessingTimeTimer(window.maxTimestamp());
    ctx.getPartitionedState(this.stateDesc).clear();
  }


  public boolean canMerge() {
    return false;
  }
}

