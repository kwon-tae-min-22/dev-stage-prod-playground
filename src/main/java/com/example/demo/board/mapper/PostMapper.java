package com.example.demo.board.mapper;

import com.example.demo.board.Post;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper {
	List<Post> selectAll();

	Post selectById(@Param("id") UUID id);

	Post insert(Post post);

	Post update(Post post);

	int delete(@Param("id") UUID id);
}
