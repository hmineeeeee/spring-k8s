package msa.api.user.controller;

import msa.api.user.vo.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

//PagingAndSortingRepository
//JpaRepository
@RepositoryRestResource
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 일반 JPQL쿼리, from뒤는 엔티티 명 (소문자로 할 시 에러)
    @Query(value = "select m from Member m where id < 3")
    public List<Member> selectAllJPQL1();

    // 일반 SQL쿼리
    @Query(value = "select m.* from Member m", nativeQuery = true)
    public List<Member> selectAllSQL1();

    // SQL 일반 파라미터 쿼리, @Param 사용 O
    @Query(value = "select m.* from Member m where id > :id", nativeQuery = true)
    public List<Member> selectSQLById2(@Param(value = "id") Long id);

    // SQL 객체 파라미터 쿼리
    @Query(value = "select m.* from Member m where id > :#{#paramMember.id}", nativeQuery = true)
    public List<Member> selectSQLById3(@Param(value = "paramMember") Member member);

}

