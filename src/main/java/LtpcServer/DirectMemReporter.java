package LtpcServer;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/11/30 17:54
 */

import Util.ReflectionUtils;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.internal.PlatformDependent;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: BG_DAQ_DataSource
 *
 * @description:
 *
 * @author: WuYe
 *
 * @create: 2020-11-30 17:54
 **/

public class DirectMemReporter {
    private AtomicLong directMem = new AtomicLong();
    private ScheduledExecutorService executor = MoreExecutors.getExitingScheduledExecutorService(
            new ScheduledThreadPoolExecutor(1), 10, TimeUnit.SECONDS);

    public DirectMemReporter() {
        Field field = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER");
        assert field != null;
        field.setAccessible(true);
        try {
            directMem = (AtomicLong) field.get(PlatformDependent.class);
        } catch (IllegalAccessException e) {
        }
    }

    public void startReport() {
        executor.scheduleAtFixedRate(() -> {
            System.out.printf("netty direct memory size:%d MB, max:%d GB\n", directMem.get()/(1024*1024), PlatformDependent.maxDirectMemory()/(1024*1024*1024));
        }, 0, 1, TimeUnit.SECONDS);
    }
}
