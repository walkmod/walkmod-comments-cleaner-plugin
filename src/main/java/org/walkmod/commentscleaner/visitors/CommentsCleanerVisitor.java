package org.walkmod.commentscleaner.visitors;

import java.beans.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.BlockComment;
import org.walkmod.javalang.ast.Comment;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.LineComment;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;

public class CommentsCleanerVisitor<T> extends VoidVisitorAdapter<T> {

   @Override
   public void visit(CompilationUnit cu, T ctx) {
      List<Comment> comments = cu.getComments();
      if (comments != null) {
         List<Comment> finalComments = new LinkedList<Comment>();
         Iterator<Comment> it = comments.iterator();

         Stack<List<Comment>> toogleComments = new Stack<List<Comment>>();

         Comment lastLineComment = null;
         while (it.hasNext()) {
            Comment comment = it.next();
            if (comment instanceof BlockComment) {
               if (!requiresToDelete(comment.getContent())) {
                  finalComments.add(comment);
               }

            } else if (comment instanceof LineComment) {
               if (lastLineComment == null || lastLineComment.getBeginLine() != comment.getBeginLine() - 1) {
                  LinkedList<Comment> list = new LinkedList<Comment>();
                  list.add(comment);
                  toogleComments.push(list);

               } else {
                  List<Comment> toogleComment = toogleComments.peek();
                  toogleComment.add(comment);

               }
               lastLineComment = comment;
            } else {

               finalComments.add(comment);
            }
         }
         Iterator<List<Comment>> itToogle = toogleComments.iterator();
         while (itToogle.hasNext()) {
            List<Comment> toogle = itToogle.next();
            String content = "";
            for (Comment c : toogle) {
               content +=  c.getContent();
            }
            if (!requiresToDelete(content)) {
               if (!finalComments.isEmpty()) {
                  Comment firstComment = toogle.get(0);
                  Iterator<Comment> fcIt = finalComments.iterator();
                  boolean added = false;
                  List<Comment> accum = new LinkedList<Comment>();
                  while (fcIt.hasNext()) {
                     Comment addedComment = fcIt.next();
                     if (addedComment.isPreviousThan(firstComment)) {
                        accum.add(addedComment);
                     } else {
                        if (!added) {
                           accum.addAll(toogle);
                           added = true;
                        }
                        accum.add(addedComment);
                     }
                  }
                  finalComments = accum;
               }
               else{
                  finalComments = toogle;
               }
            }
            
         }

         cu.setComments(finalComments);
      }
   }

   private boolean requiresToDelete(String code) {
      try {
         if(!code.startsWith("{")){
            code = "{"+code+"}";
         }
         ASTManager.parse(Statement.class, code);
         return true;
      } catch (ParseException e) {
      }
      return false;
   }

}
