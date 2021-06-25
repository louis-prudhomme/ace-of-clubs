package org.example.aofc.utils;

import lombok.NonNull;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SyncingQueue<T> {
  @NonNull private final List<T> underlying = new ArrayList<>();

  public void push(T t) {
    this.underlying.add(Optional.ofNullable(t).orElseThrow(InvalidParameterException::new));
  }

  public @NonNull Optional<T> pop() {
    if (underlying.isEmpty()) return Optional.empty();

    T res = underlying.get(0);
    underlying.remove(res);

    return Optional.of(res);
  }

  public Optional<T> peek() {
    if (underlying.isEmpty()) return Optional.empty();

    return Optional.of(underlying.get(0));
  }

  public int size() {
    return underlying.size();
  }

  public boolean isEmpty() {
    return underlying.isEmpty();
  }

  public List<T> toList() {
    return List.copyOf(this.underlying);
  }
}
