package com.contentful.structured.core;

import com.contentful.java.cda.structured.CDAStructuredNode;
import com.contentful.java.cda.structured.CDAStructuredParagraph;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This base class for all processing needed to be done on a structured text type. It will walk
 * through all the nodes of a structured text element and call associated node renderer and
 * checkers on it.
 * <p>
 * Use {@see #addRenderer} for adding more low level renders on it or use one of the extension
 * modules.
 *
 * @param <C> the context type used throughout this rendering process.
 * @param <R> the result type of the rendering process.
 * @see Renderer
 * @see Checker
 */
public class Processor<C extends Context, R> {

  @Nonnull
  private final List<CheckingRenderer<C, R>> nodeRenderer = new ArrayList<>();
  @Nonnull
  private C context;
  @Nonnull
  private List<Integer> childrenPerDepth = new ArrayList<>();

  /**
   * Create a new empty processor.
   */
  public Processor(@Nonnull C context) {
    childrenPerDepth.add(0);
    this.context = context;
  }

  /**
   * Add a renderer to the end of the list of available renders.
   * <p>
   * If a renderer before hand matches the node, the new renderer might not be executed. If you want
   * to make sure your renderer is considered first, either {@see #reset} the list of renderer, or
   * {@see #addRendererUpFront} to make your renderer the first to be checked, overriding other
   * renderer added.
   *
   * @param renderer may not be null.
   * @return this renderer for chaining.
   */
  @Nonnull
  public Processor addRenderer(@Nonnull Checker<C> checker, @Nonnull Renderer<C, R> renderer) {
    nodeRenderer.add(new CheckingRenderer<>(checker, renderer));
    return this;
  }

  /**
   * Adds the renderer to the front of the list of renderer, overriding similar renderer if needed.
   *
   * @param renderer may not be null.
   * @return this renderer for chaining.
   */
  @Nonnull
  public Processor addRendererUpFront(@Nonnull Checker<C> checker, @Nonnull Renderer<C, R> renderer) {
    nodeRenderer.add(0, new CheckingRenderer<>(checker, renderer));
    return this;
  }

  /**
   * Start the node rendering process.
   * <p>
   * This process will render the node and all of it's child nodes by using the added renderer.
   *
   * @param node a not null node to be rendered.
   * @return the result in the form given by the renderer.
   */
  @Nullable public R render(@Nonnull CDAStructuredNode node) {
    if (node instanceof CDAStructuredParagraph) {
      context.onParagraphEntered((CDAStructuredParagraph) node);
      childrenPerDepth.add(0);
    }

    R result = null;
    for (final CheckingRenderer<C, R> pair : nodeRenderer) {
      final Checker<C> checker = pair.checker;

      if (checker.check(context, node)) {
        final int lastChildIndex = childrenPerDepth.size() - 1;
        final Integer lastChildrenCount = childrenPerDepth.get(lastChildIndex);
        context.onSiblingEncountered(node, lastChildrenCount);
        childrenPerDepth.set(lastChildIndex, lastChildrenCount + 1);

        final Renderer<C, R> renderer = pair.renderer;
        result = renderer.render(context, node);
        if (result != null) {
          break;
        }
      }
    }

    if (node instanceof CDAStructuredParagraph) {
      context.onParagraphExited((CDAStructuredParagraph) node);
      childrenPerDepth.remove(childrenPerDepth.size() - 1);
    }

    return result;
  }

  /**
   * Internal class for combining checkers and renderer.
   *
   * @param <C> Custom context class
   * @param <R> Result class of the rendering
   */
  private static class CheckingRenderer<C, R> {
    final Checker<C> checker;
    final Renderer<C, R> renderer;

    /**
     * Construct a new CheckerRenderer
     *
     * @param checker  the checker to be used
     * @param renderer the renderer to be checked
     */
    CheckingRenderer(@Nonnull Checker<C> checker, @Nonnull Renderer<C, R> renderer) {
      this.checker = checker;
      this.renderer = renderer;
    }
  }
}
