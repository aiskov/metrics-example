package demo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "query/tasks")
public class TaskQueryController {
    private final TaskRepository taskRepository;
    private final Counter queryCounter;

    public TaskQueryController(MeterRegistry registry, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

        this.queryCounter = Counter.builder("app.query.count")
                .baseUnit("count")
                .register(registry);
    }

    @GetMapping
    TaskListResponse taskList() {
        this.queryCounter.increment();

        return TaskListResponse.builder()
                .tasks(this.taskRepository.findAll().stream()
                        .map(task -> TaskListResponseItem.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .tags(task.tagsAsSet())
                                .build()
                        )
                        .collect(toList())
                )
                .build();
    }

    @Value @Builder
    public static class TaskListResponse {
        List<TaskListResponseItem> tasks;

        public List<TaskListResponseItem> getTasks() {
            return unmodifiableList(tasks);
        }
    }

    @Value @Builder
    public static class TaskListResponseItem {
        String id;
        String title;
        Set<String> tags;

        public Set<String> getTags() {
            return unmodifiableSet(tags);
        }
    }
}
