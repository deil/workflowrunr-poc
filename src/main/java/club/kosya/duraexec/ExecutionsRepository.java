package club.kosya.duraexec;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionsRepository extends JpaRepository<Execution, Long> {
}
