package demo.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class BackgroundMetricCollector {
    private final AtomicLong tasksGauge;
    private final TaskRepository taskRepository;

    public BackgroundMetricCollector(MeterRegistry registry, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.tasksGauge = registry.gauge("app.tasks.background.count", new AtomicLong(taskRepository.count()));
    }

    @Scheduled(fixedRate = 5_000)
    public void collect() {
        this.tasksGauge.set(this.taskRepository.count());
    }
}
