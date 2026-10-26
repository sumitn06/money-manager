package moneymanager.repository;

import moneymanager.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // select * from tbl_category where profileId = ?1
    List<CategoryEntity> findByProfileId(Long ProfileId);

    // select * from tbl_category where id = ?1 and profileId = ?2
    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);

    // select * from tbl_category where type = ?1 and profileId = ?2
    List<CategoryEntity> findByTypeAndProfileId(String type, Long profileId);

    Boolean existsByNameAndProfileId(String name, Long profileId);
}
