package club.kosya.duraexec.internal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionsRepository extends JpaRepository<Execution, Long> {
}
