package club.kosya.duraexec.internal

import org.springframework.data.jpa.repository.JpaRepository

interface ExecutionsRepository : JpaRepository<Execution, Long>
