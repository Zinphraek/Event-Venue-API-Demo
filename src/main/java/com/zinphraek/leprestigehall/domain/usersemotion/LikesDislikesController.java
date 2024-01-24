package com.zinphraek.leprestigehall.domain.usersemotion;

import static com.zinphraek.leprestigehall.domain.constants.Paths.CommentsLikesDislikesPath;
import static com.zinphraek.leprestigehall.domain.constants.Paths.LikesDislikesPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;

@Controller
public class LikesDislikesController {

  @Autowired private final LikesDislikesServiceImplementation likesDislikesService;

  public LikesDislikesController(LikesDislikesServiceImplementation likesDislikesService) {
    this.likesDislikesService = likesDislikesService;
  }

  @DeleteMapping(LikesDislikesPath + "/{id}")
  public ResponseEntity<Void> deleteEventLikesDislikes(@PathVariable("id") Long id) {
    likesDislikesService.deleteEventLikesDislikes(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping(CommentsLikesDislikesPath + "/{id}")
  public ResponseEntity<Void> deleteCommentLikesDislikes(@PathVariable("id") Long id) {
    likesDislikesService.deleteCommentLikesDislikes(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(LikesDislikesPath)
  public ResponseEntity<EventLikesDislikes> saveEventLikesDislikes(
      @RequestPart("likes") EventLikesDislikesDTO eventLikesDislikesDTO) {
    return new ResponseEntity<>(
        likesDislikesService.saveEventLikesDislikes(eventLikesDislikesDTO), HttpStatus.CREATED);
  }

  @PostMapping(CommentsLikesDislikesPath)
  public ResponseEntity<CommentLikesDislikes> saveCommentLikesDislikes(
      @RequestPart("likes") CommentLikesDislikesDTO commentLikesDislikesDTO) {
    return new ResponseEntity<>(
        likesDislikesService.saveCommentLikesDislikes(commentLikesDislikesDTO), HttpStatus.CREATED);
  }

  @PutMapping(LikesDislikesPath + "/{id}")
  public ResponseEntity<EventLikesDislikes> updateEventLikesDislikes(
      @PathVariable("id") Long id,
      @RequestPart("likes") EventLikesDislikesDTO eventLikesDislikesDTO) {
    return new ResponseEntity<>(
        likesDislikesService.updateEventLikesDislikes(id, eventLikesDislikesDTO), HttpStatus.OK);
  }

  @PutMapping(CommentsLikesDislikesPath + "/{id}")
  public ResponseEntity<CommentLikesDislikes> updateCommentLikesDislikes(
      @PathVariable("id") Long id,
      @RequestPart("likes") CommentLikesDislikesDTO commentLikesDislikesDTO) {
    return new ResponseEntity<>(
        likesDislikesService.updateCommentLikesDislikes(id, commentLikesDislikesDTO),
        HttpStatus.OK);
  }
}
