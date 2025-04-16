package com.tasktracker.collections;

import java.util.*;

public class CustomLinkedHashMap<K, V>
    implements SequencedMap<K, V>, Iterable<V>, Comparable<CustomLinkedHashMap<K, V>> {
  public static final String MAP_IS_EMPTY_GET_LAST_VALUE = "Map is empty (getLast value)";
  public static final String MAP_IS_EMPTY_GET_FIRST_VALUE = "Map is empty (getFirst value)";
  public static final String KEY_CAN_T_BE_NULL = "key can't be null";
  public static final String VALUE_CAN_T_BE_NULL = "value can't be null";
  public static final String NODE_CAN_T_BE_NULL = "node can't be null";
  public static final String MAP_IS_EMPTY_GET_FIRST_KEY = "Map is empty (getFirst key)";
  private final HashMap<K, Node<K, V>> hashMap = new HashMap<>();
  private EntrySetView entrySetView = null;
  private ReversedCustomLinkedHashMapView<K, V> reversedMapView = null;
  private ValuesView valuesView = null;
  private KeySetView keySetView = null;
  private Node<K, V> lastNode = null;
  private Node<K, V> firstNode = null;

  @Override
  public SequencedMap<K, V> reversed() {
    if (reversedMapView == null) {
      reversedMapView = new ReversedCustomLinkedHashMapView<>(this);
    }
    return reversedMapView;
  }

  @Override
  public Entry<K, V> firstEntry() {
    if (firstNode == null) return null;
    return firstNode.getImmutableEntry();
  }

  @Override
  public Entry<K, V> lastEntry() {
    if (lastNode == null) return null;
    return lastNode.getImmutableEntry();
  }

  private AbstractMap.SimpleImmutableEntry<K, V> pollNodeAndGetEntry(Node<K, V> node) {
    if (node == null) return null;
    unlinkNode(node);
    hashMap.remove(node.getKey());
    return node.getImmutableEntry();
  }

  @Override
  public Entry<K, V> pollFirstEntry() {
    return pollNodeAndGetEntry(firstNode);
  }

  @Override
  public Entry<K, V> pollLastEntry() {
    return pollNodeAndGetEntry(lastNode);
  }

  private V putInPosition(K k, V v, boolean atFront) {
    Objects.requireNonNull(k, KEY_CAN_T_BE_NULL);
    Objects.requireNonNull(v, VALUE_CAN_T_BE_NULL);
    final var replacedNode = hashMap.get(k);
    if (replacedNode != null) {
      var oldValue = replacedNode.getValue();
      replacedNode.setValue(v);
      return oldValue;
    }
    final var newNode = new Node<>(k, v, null, null);
    if (hashMap.isEmpty()) {
      firstNode = newNode;
      lastNode = newNode;
      hashMap.put(k, newNode);
    } else {
      if (atFront) {
        firstNode.setPrevious(newNode);
        newNode.setNext(firstNode);
        firstNode = newNode;
      } else {
        lastNode.setNext(newNode);
        newNode.setPrevious(lastNode);
        lastNode = newNode;
      }
      hashMap.put(k, newNode);
    }
    return null;
  }

  @Override
  public V putFirst(K k, V v) {
    return putInPosition(k, v, true);
  }

  @Override
  public V putLast(K k, V v) {
    return putInPosition(k, v, false);
  }

  @Override
  public int size() {
    return hashMap.size();
  }

  @Override
  public boolean isEmpty() {
    return hashMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    Objects.requireNonNull(key, KEY_CAN_T_BE_NULL);
    return hashMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    Objects.requireNonNull(value, VALUE_CAN_T_BE_NULL);
    for (Node<K, V> node = firstNode; node != null; node = node.getNext()) {
      if (Objects.equals(node.getValue(), value)) return true;
    }
    return false;
  }

  @Override
  public V get(Object key) {
    Objects.requireNonNull(key, KEY_CAN_T_BE_NULL);
    Node<K, V> node = hashMap.get(key);
    return (node != null) ? node.getValue() : null;
  }

  @Override
  public V put(K key, V value) {
    Objects.requireNonNull(key, KEY_CAN_T_BE_NULL);
    Objects.requireNonNull(value, VALUE_CAN_T_BE_NULL);
    var newNode = new Node<>(key, value, null, lastNode);
    if (hashMap.isEmpty()) {
      hashMap.put(key, newNode);
      lastNode = newNode;
      firstNode = newNode;
      return null;
    }
    var removedNode = hashMap.put(key, newNode);
    if (removedNode != null) {
      replaceNode(removedNode, newNode);
      return removedNode.getValue();
    }
    lastNode.setNext(newNode);
    lastNode = newNode;
    return null;
  }

  @Override
  public V remove(Object key) {
    Objects.requireNonNull(key, KEY_CAN_T_BE_NULL);
    Node<K, V> removedNode = hashMap.remove(key);
    if (removedNode != null) {
      unlinkNode(removedNode);
      return removedNode.getValue();
    }
    return null;
  }

  private void unlinkNode(Node<K, V> node) {
    Objects.requireNonNull(node, NODE_CAN_T_BE_NULL);
    var previousNode = node.getPrevious();
    var nextNode = node.getNext();
    if (previousNode != null) {
      previousNode.setNext(nextNode);
    } else {
      firstNode = nextNode;
    }
    if (nextNode != null) {
      nextNode.setPrevious(previousNode);
    } else {
      lastNode = previousNode;
    }
  }

  private void replaceNode(Node<K, V> nodeToUnlink, Node<K, V> nodeToInsert) {
    Objects.requireNonNull(nodeToUnlink, "nodeToUnlink can't be Null");
    Objects.requireNonNull(nodeToInsert, "nodeToInsert can't be Null");
    var previousNode = nodeToUnlink.getPrevious();
    var nextNode = nodeToUnlink.getNext();
    if (previousNode != null) {
      previousNode.setNext(nodeToInsert);
      nodeToInsert.setPrevious(previousNode);
    } else {
      firstNode = nodeToInsert;
    }
    if (nextNode != null) {
      nextNode.setPrevious(nodeToInsert);
      nodeToInsert.setNext(nextNode);
    } else {
      lastNode = nodeToInsert;
    }
  }

  @Override
  public void clear() {
    hashMap.clear();
    firstNode = null;
    lastNode = null;
    reversedMapView = null;
    valuesView = null;
    entrySetView = null;
    keySetView = null;
  }

  @Override
  public SequencedSet<K> keySet() {
    if (keySetView == null) {
      keySetView = new KeySetView();
    }
    return keySetView;
  }

  @Override
  public SequencedCollection<V> values() {
    if (valuesView == null) {
      valuesView = new ValuesView();
    }
    return valuesView;
  }

  @Override
  public SequencedSet<Entry<K, V>> entrySet() {
    if (entrySetView == null) {
      entrySetView = new EntrySetView();
    }
    return entrySetView;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    Objects.requireNonNull(m);
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      this.putLast(e.getKey(), e.getValue());
    }
  }

  @Override
  public Iterator<V> iterator() {
    return new LinkedIterator<>(firstNode, true, Node::getValue);
  }

  @Override
  public int compareTo(CustomLinkedHashMap<K, V> o) {
    Objects.requireNonNull(o, "Cannot compare to a null map");
    return Integer.compare(this.size(), o.size());
  }

  private <T> T getElementFromSequencedNode(
      Node<K, V> node,
      java.util.function.Function<Node<K, V>, T> extractor,
      String exceptionMessage) {
    if (node == null) {
      throw new NoSuchElementException(exceptionMessage);
    }
    return extractor.apply(node);
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (Map.Entry<K, V> entry : entrySet()) {
      h += entry.hashCode();
    }
    return h;
  }

  @Override
  public boolean remove(Object key, Object value) {
    Objects.requireNonNull(key, KEY_CAN_T_BE_NULL);
    Node<K, V> node = hashMap.get(key);
    if (node != null && Objects.equals(node.getValue(), value)) {
      remove(key);
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof Map<?, ?> m)) return false;
    if (m.size() != size()) return false;
    try {
      for (Map.Entry<K, V> e : entrySet()) {
        K key = e.getKey();
        V value = e.getValue();
        if (value == null) {
          if (!(m.get(key) == null && m.containsKey(key))) return false;
        } else {
          if (!value.equals(m.get(key))) return false;
        }
      }
    } catch (ClassCastException | NullPointerException unused) {
      return false;
    }
    return true;
  }

  private static class Node<K, V> {
    private final K key;

    private V value;
    private Node<K, V> next;
    private Node<K, V> previous;

    public Node(K key, V value, Node<K, V> next, Node<K, V> previous) {
      this.key = key;
      this.value = value;
      this.next = next;
      this.previous = previous;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public void setValue(V value) {
      this.value = value;
    }

    public AbstractMap.SimpleImmutableEntry<K, V> getImmutableEntry() {
      return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public Node<K, V> getNext() {
      return next;
    }

    public void setNext(Node<K, V> next) {
      this.next = next;
    }

    public Node<K, V> getPrevious() {
      return previous;
    }

    public void setPrevious(Node<K, V> previous) {
      this.previous = previous;
    }
  }

  private static final class ReversedCustomLinkedHashMapView<K, V> implements SequencedMap<K, V> {
    private final CustomLinkedHashMap<K, V> originalMap;

    ReversedCustomLinkedHashMapView(CustomLinkedHashMap<K, V> original) {
      Objects.requireNonNull(original, "Original map cannot be null for ReversedMapView");
      this.originalMap = original;
    }

    @Override
    public SequencedMap<K, V> reversed() {
      return originalMap;
    }

    @Override
    public Entry<K, V> firstEntry() {
      return originalMap.lastEntry();
    }

    @Override
    public Entry<K, V> lastEntry() {
      return originalMap.firstEntry();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
      return originalMap.pollLastEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
      return originalMap.pollFirstEntry();
    }

    @Override
    public V putFirst(K k, V v) {
      return originalMap.putLast(k, v);
    }

    @Override
    public V putLast(K k, V v) {
      return originalMap.putFirst(k, v);
    }

    @Override
    public int size() {
      return originalMap.size();
    }

    @Override
    public boolean isEmpty() {
      return originalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      return originalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      return originalMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
      return originalMap.get(key);
    }

    @Override
    public V put(K key, V value) {
      return originalMap.putFirst(key, value);
    }

    @Override
    public V remove(Object key) {
      return originalMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
      Objects.requireNonNull(m);
      for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
        this.put(e.getKey(), e.getValue());
      }
    }

    @Override
    public void clear() {
      originalMap.clear();
    }

    @Override
    public Set<K> keySet() {
      return originalMap.keySet().reversed();
    }

    @Override
    public Collection<V> values() {
      return originalMap.values().reversed();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return originalMap.entrySet().reversed();
    }

    @Override
    public boolean remove(Object key, Object value) {
      return originalMap.remove(key, value);
    }

    @Override
    public int hashCode() {
      return originalMap.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (!(obj instanceof Map<?, ?>)) return false;
      return originalMap.equals(obj);
    }
  }

  private class ValuesView extends AbstractCollection<V> implements SequencedCollection<V> {
    private ReversedValuesView reversedValuesView = null;

    @Override
    public Iterator<V> iterator() {
      return new LinkedIterator<>(CustomLinkedHashMap.this.firstNode, true, Node::getValue);
    }

    @Override
    public int size() {
      return CustomLinkedHashMap.this.size();
    }

    @Override
    public boolean contains(Object o) {
      return CustomLinkedHashMap.this.containsValue(o);
    }

    @Override
    public void clear() {
      CustomLinkedHashMap.this.clear();
    }

    @Override
    public SequencedCollection<V> reversed() {
      if (reversedValuesView == null) {
        reversedValuesView = new ReversedValuesView();
      }
      return reversedValuesView;
    }

    @Override
    public V getFirst() {
      return getElementFromSequencedNode(firstNode, Node::getValue, MAP_IS_EMPTY_GET_FIRST_VALUE);
    }

    @Override
    public V getLast() {
      return getElementFromSequencedNode(lastNode, Node::getValue, MAP_IS_EMPTY_GET_LAST_VALUE);
    }

    private class ReversedValuesView extends AbstractCollection<V>
        implements SequencedCollection<V> {
      @Override
      public Iterator<V> iterator() {
        return new LinkedIterator<>(CustomLinkedHashMap.this.lastNode, false, Node::getValue);
      }

      @Override
      public int size() {
        return CustomLinkedHashMap.this.size();
      }

      @Override
      public SequencedCollection<V> reversed() {
        return ValuesView.this;
      }

      @Override
      public V getFirst() {
        return getElementFromSequencedNode(
            CustomLinkedHashMap.this.lastNode, Node::getValue, MAP_IS_EMPTY_GET_FIRST_VALUE);
      }

      @Override
      public V getLast() {
        return getElementFromSequencedNode(
            CustomLinkedHashMap.this.firstNode, Node::getValue, MAP_IS_EMPTY_GET_LAST_VALUE);
      }
    }
  }

  private final class KeySetView extends AbstractSet<K> implements SequencedSet<K> {
    public static final String MAP_IS_EMPTY_GET_LAST_KEY = "Map is empty (getLast key)";
    ReversedKeySetView reversedKeySetView = null;

    @Override
    public Iterator<K> iterator() {
      return new LinkedIterator<>(CustomLinkedHashMap.this.firstNode, true, Node::getKey);
    }

    @Override
    public int size() {
      return CustomLinkedHashMap.this.size();
    }

    @Override
    public boolean contains(Object o) {
      return CustomLinkedHashMap.this.containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
      final boolean contained = CustomLinkedHashMap.this.containsKey(o);
      if (contained) {
        CustomLinkedHashMap.this.remove(o);
      }
      return contained;
    }

    @Override
    public void clear() {
      CustomLinkedHashMap.this.clear();
    }

    @Override
    public SequencedSet<K> reversed() {
      if (reversedKeySetView == null) {
        reversedKeySetView = new ReversedKeySetView();
      }
      return reversedKeySetView;
    }

    @Override
    public K getFirst() {
      return getElementFromSequencedNode(
          CustomLinkedHashMap.this.firstNode, Node::getKey, MAP_IS_EMPTY_GET_FIRST_KEY);
    }

    @Override
    public K getLast() {
      return getElementFromSequencedNode(
          CustomLinkedHashMap.this.lastNode, Node::getKey, MAP_IS_EMPTY_GET_LAST_KEY);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    private final class ReversedKeySetView extends AbstractSet<K> implements SequencedSet<K> {
      @Override
      public Iterator<K> iterator() {
        return new LinkedIterator<>(CustomLinkedHashMap.this.lastNode, false, Node::getKey);
      }

      @Override
      public int size() {
        return CustomLinkedHashMap.this.size();
      }

      @Override
      public boolean contains(Object o) {
        return CustomLinkedHashMap.this.containsKey(o);
      }

      @Override
      public boolean remove(Object o) {
        final boolean contains = CustomLinkedHashMap.this.containsKey(o);
        if (contains) {
          CustomLinkedHashMap.this.remove(o);
        }
        return contains;
      }

      @Override
      public void clear() {
        CustomLinkedHashMap.this.clear();
      }

      @Override
      public SequencedSet<K> reversed() {
        return CustomLinkedHashMap.this.keySet();
      }

      @Override
      public K getFirst() {
        return getElementFromSequencedNode(
            CustomLinkedHashMap.this.lastNode, Node::getKey, MAP_IS_EMPTY_GET_FIRST_KEY);
      }

      @Override
      public K getLast() {
        return getElementFromSequencedNode(
            CustomLinkedHashMap.this.firstNode, Node::getKey, MAP_IS_EMPTY_GET_LAST_KEY);
      }
    }
  }

  private class EntrySetView extends AbstractSet<Map.Entry<K, V>>
      implements SequencedSet<Map.Entry<K, V>> {
    private ReversedEntrySetView reversedEntrySetView = null;

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new LinkedIterator<>(
          CustomLinkedHashMap.this.firstNode, true, Node::getImmutableEntry);
    }

    @Override
    public int size() {
      return CustomLinkedHashMap.this.size();
    }

    @Override
    public boolean contains(Object o) {
      return containsEntry(o);
    }

    private boolean containsEntry(Object o) {
      if (!(o instanceof Map.Entry<?, ?>)) {
        return false;
      }
      Object key = ((Entry<?, ?>) o).getKey();
      Node<K, V> node = CustomLinkedHashMap.this.hashMap.get(key);
      return node != null && Objects.equals(node.getValue(), ((Entry<?, ?>) o).getValue());
    }

    @Override
    public boolean remove(Object o) {
      return removeEntry(o);
    }

    private boolean removeEntry(Object o) {
      if (!(o instanceof Map.Entry<?, ?> entry)) {
        return false;
      }
      Object key = entry.getKey();
      Node<K, V> node = CustomLinkedHashMap.this.hashMap.get(key);

      if (node != null && Objects.equals(node.getValue(), entry.getValue())) {
        CustomLinkedHashMap.this.remove(key);
        return true;
      }
      return false;
    }

    @Override
    public void clear() {
      CustomLinkedHashMap.this.clear();
    }

    @Override
    public SequencedSet<Map.Entry<K, V>> reversed() {
      if (reversedEntrySetView == null) {
        reversedEntrySetView = new ReversedEntrySetView();
      }
      return reversedEntrySetView;
    }

    @Override
    public Map.Entry<K, V> getFirst() {
      return getElementFromSequencedNode(
          CustomLinkedHashMap.this.firstNode,
          Node::getImmutableEntry,
          "Map is empty (getFirst entry)");
    }

    @Override
    public Map.Entry<K, V> getLast() {
      return getElementFromSequencedNode(
          CustomLinkedHashMap.this.lastNode,
          Node::getImmutableEntry,
          "Map is empty (getLast entry)");
    }

    private final class ReversedEntrySetView extends AbstractSet<Map.Entry<K, V>>
        implements SequencedSet<Map.Entry<K, V>> {

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return new LinkedIterator<>(
            CustomLinkedHashMap.this.lastNode, false, Node::getImmutableEntry);
      }

      @Override
      public int size() {
        return CustomLinkedHashMap.this.size();
      }

      @Override
      public boolean contains(Object o) {
        return containsEntry(o);
      }

      @Override
      public boolean remove(Object o) {
        return removeEntry(o);
      }

      @Override
      public void clear() {
        CustomLinkedHashMap.this.clear();
      }

      @Override
      public SequencedSet<Entry<K, V>> reversed() {
        return CustomLinkedHashMap.this.entrySet();
      }

      @Override
      public Entry<K, V> getFirst() {
        return getElementFromSequencedNode(
            CustomLinkedHashMap.this.lastNode,
            Node::getImmutableEntry,
            "Map is empty (getFirst entry)");
      }

      @Override
      public Entry<K, V> getLast() {
        return getElementFromSequencedNode(
            CustomLinkedHashMap.this.firstNode,
            Node::getImmutableEntry,
            "Map is empty (getLast entry)");
      }
    }
  }

  private class LinkedIterator<T> implements Iterator<T> {
    private final boolean forward;
    private final java.util.function.Function<Node<K, V>, T> elementExtractor;
    private Node<K, V> nextNode;
    private Node<K, V> lastReturnedNode;

    LinkedIterator(
        Node<K, V> startNode,
        boolean forward,
        java.util.function.Function<Node<K, V>, T> elementExtractor) {
      this.nextNode = startNode;
      this.forward = forward;
      this.elementExtractor = elementExtractor;
      this.lastReturnedNode = null;
    }

    @Override
    public final boolean hasNext() {
      return nextNode != null;
    }

    @Override
    public final T next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more elements");
      }
      lastReturnedNode = nextNode;
      T element = elementExtractor.apply(nextNode);
      nextNode = forward ? nextNode.getNext() : nextNode.getPrevious();
      return element;
    }

    @Override
    public final void remove() {
      if (lastReturnedNode == null) {
        throw new IllegalStateException(
            "next() must be called before remove(), or remove() called twice");
      }
      CustomLinkedHashMap.this.remove(lastReturnedNode.getKey());
      lastReturnedNode = null;
    }
  }
}
