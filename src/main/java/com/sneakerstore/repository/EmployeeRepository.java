package com.sneakerstore.repository;

import com.sneakerstore.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findWithUserById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findByUserUsername(String username);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT e FROM Employee e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR e.status = :status)")
    Page<Employee> searchEmployees(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Employee> findAllByOrderByIdAsc();
}

