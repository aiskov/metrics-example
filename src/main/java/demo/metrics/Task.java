package demo.metrics;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

@Data
@Entity
public class Task {
    @Id
    private String id;
    private String title;
    private String tags;

    public Set<String> tagsAsSet() {
        if (this.tags == null) return Set.of();
        return Stream.of(this.tags.split(","))
                .map(String::trim)
                .collect(toCollection(TreeSet::new));
    }

    public void tagsFromSet(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            this.tags = null;
            return;
        }

        this.tags = String.join(",", tags);
    }
}
