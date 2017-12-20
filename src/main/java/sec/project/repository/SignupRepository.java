package sec.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sec.project.domain.Account;

public interface SignupRepository extends JpaRepository<Account, Long> {

    /*
    fail
    //@Query(value = "?1", nativeQuery = true)
    @Query(value = "SELECT * FROM Account WHERE NAME = ?1", nativeQuery = true)
    Account findByEmailAddress(String foo);

    // "select u from User u where u.emailAddress = ?1"
    @Query(value = "SELECT u FROM Account u WHERE u.NAME = SomeName")
    Account noparam();
    */

}
