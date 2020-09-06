package demo.metrics.api;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.springframework.data.repository.CrudRepository;
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

    private final Counter addCommandsCounter;
    private final Counter updateCommandsCounter;
    private final Counter deleteCommandsCounter;

    private final Timer addCommandsTimer;
    private final Timer updateCommandsTimer;
    private final Timer deleteCommandsTimer;

    private AtomicLong tasksGauge;

    public TaskCommandController(MeterRegistry registry, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

        // Counter needed?
        this.addCommandsCounter = registry.counter("app.command.counter", Tags.of(Tag.of("command", "add")));
        this.updateCommandsCounter = registry.counter("app.command.counter", Tags.of(Tag.of("command", "update")));
        this.deleteCommandsCounter = registry.counter("app.command.counter", Tags.of(Tag.of("command", "delete")));

        this.addCommandsTimer = registry.timer("app.command.timer", Tags.of(Tag.of("command", "add")));
        this.updateCommandsTimer = registry.timer("app.command.timer", Tags.of(Tag.of("command", "update")));
        this.deleteCommandsTimer = registry.timer("app.command.timer", Tags.of(Tag.of("command", "delete")));

        // Does Gauge is good idea?
        this.tasksGauge = registry.gauge("app.tasks.count", new AtomicLong(taskRepository.count()));
        registry.gauge("app.tasks.db-count", taskRepository, CrudRepository::count);
    }

    @PostMapping("add")
    SuccessResponse add(@RequestBody AddTaskCommand command) {
        this.addCommandsCounter.increment();
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
        this.updateCommandsCounter.increment();
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
        this.deleteCommandsCounter.increment();
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
