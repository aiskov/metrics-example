package demo.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RestController
@RequestMapping(path = "command/tasks")
public class TaskCommandController {
    private final TaskRepository taskRepository;

    private final Timer addCommandsTimer;
    private final Timer updateCommandsTimer;
    private final Timer deleteCommandsTimer;

    private AtomicLong tasksGauge;

    public TaskCommandController(MeterRegistry registry, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

        this.addCommandsTimer = Timer.builder("app.command.timer")
                .tag("command", "add")
                .publishPercentiles(0.05, 0.5, 0.75, 0.95)
                .register(registry);

        this.updateCommandsTimer = Timer.builder("app.command.timer")
                .tag("command", "update")
                .publishPercentiles(0.05, 0.5, 0.75, 0.95)
                .register(registry);

        this.deleteCommandsTimer = Timer.builder("app.command.timer")
                .tag("command", "delete")
                .publishPercentiles(0.05, 0.5, 0.75, 0.95)
                .register(registry);

        // Does Gauge is good idea?
        this.tasksGauge = new AtomicLong(taskRepository.count());

        Gauge.builder("app.tasks.count", this.tasksGauge::get)
                .baseUnit("count")
                .register(registry);

        Gauge.builder("app.tasks.db-count", taskRepository::count)
                .baseUnit("count")
                .register(registry);
    }

    @PostMapping("add")
    SuccessResponse add(@RequestBody AddTaskCommand command) {
        long startProcessing = currentTimeMillis();

        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setTitle(command.title);
        task.tagsFromSet(command.tags);
        this.taskRepository.save(task);

        this.tasksGauge.incrementAndGet();
        this.addCommandsTimer.record(currentTimeMillis() - startProcessing, MILLISECONDS);

        return SuccessResponse.builder().id(task.getId()).build();
    }

    @PostMapping("update")
    SuccessResponse update(@RequestBody UpdateTaskCommand command) {
        long startProcessing = currentTimeMillis();

        Task task = this.taskRepository.getOne(command.getId());
        task.setTitle(command.title);
        task.tagsFromSet(command.tags);

        this.taskRepository.save(task);

        this.updateCommandsTimer.record(currentTimeMillis() - startProcessing, MILLISECONDS);

        return SuccessResponse.builder().id(task.getId()).build();
    }

    @PostMapping("delete")
    SuccessResponse delete(@RequestBody DeleteTaskCommand command) {
        long startProcessing = currentTimeMillis();

        this.taskRepository.deleteById(command.getId());

        this.tasksGauge.decrementAndGet();
        this.deleteCommandsTimer.record(currentTimeMillis() - startProcessing, MILLISECONDS);

        return SuccessResponse.builder().id(command.getId()).build();
    }

    @PostMapping("broken")
    SuccessResponse broken() {
        throw new IllegalStateException();
    }

    @Data
    public static class AddTaskCommand {
        private String title;
        private Set<String> tags;
    }

    @Data
    public static class UpdateTaskCommand {
        private String id;
        private String title;
        private Set<String> tags;
    }

    @Data
    public static class DeleteTaskCommand {
        private String id;
    }

    @Value @Builder
    public static class SuccessResponse {
        String id;
    }
}
