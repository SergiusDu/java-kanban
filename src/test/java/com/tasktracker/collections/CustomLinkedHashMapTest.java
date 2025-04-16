package com.tasktracker.collections; // Убедись, что пакет правильный

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;

/** Comprehensive tests for the CustomLinkedHashMap class. */
class CustomLinkedHashMapTest {

  private static final Integer K1 = 1;
  private static final Integer K2 = 2;
  private static final Integer K3 = 3;
  private static final Integer K4 = 4;
  private static final String V1 = "Value 1";
  private static final String V2 = "Value 2";
  private static final String V3 = "Value 3";
  private static final String V4 = "Value 4";
  private static final String V_REPLACE = "Replaced Value";
  private static final Integer K_NON_EXIST = 999;
  private static final String V_NON_EXIST = "NonExistentValue";
  private CustomLinkedHashMap<Integer, String> map;
  private CustomLinkedHashMap<Integer, String> mapEmpty;
  private CustomLinkedHashMap<Integer, String> mapSingleElement;

  @BeforeEach
  void setUp() {
    mapEmpty = new CustomLinkedHashMap<>();
    map = new CustomLinkedHashMap<>();
    map.put(K1, V1);
    map.put(K2, V2);
    map.put(K3, V3);
    // mapStringKeys = new CustomLinkedHashMap<>(); // Не используется
    mapSingleElement = new CustomLinkedHashMap<>();
    mapSingleElement.put(K1, V1);
  }

  // =====================================================
  // Basic Map Operations Tests
  // =====================================================

  // Generic test for basic view properties
  private <T> void testViewBasics(
      Collection<T> view, T existingElement, T nonExistingElement, int expectedSize) {
    assertNotNull(view, "View should not be null");
    assertEquals(expectedSize, view.size(), "View size should match map size");
    assertEquals(map.isEmpty(), view.isEmpty(), "View isEmpty should match map isEmpty");

    // Contains checks
    if (existingElement != null) {
      assertTrue(
          view.contains(existingElement),
          "View should contain existing element: " + existingElement);
    }
    if (nonExistingElement != null) {
      assertFalse(
          view.contains(nonExistingElement),
          "View should not contain non-existing element: " + nonExistingElement);
    }

    // Test clear via view
    if (!view.isEmpty()) {
      // Create a temporary map to test clear on, preserving 'map' for other tests if needed
      CustomLinkedHashMap<Integer, String> tempMap = createCopy(map);
      Collection<T> tempView = getViewForMap(tempMap, view.getClass());

      assertFalse(tempMap.isEmpty());
      assertFalse(tempView.isEmpty());
      tempView.clear();
      assertTrue(tempMap.isEmpty(), "Map should be empty after clearing view");
      assertTrue(tempView.isEmpty(), "View should be empty after clearing view");
      assertEquals(0, tempView.size(), "View size should be 0 after clearing");
    } else {
      view.clear();
      assertTrue(view.isEmpty());
      assertEquals(0, view.size());
    }
  }

  // Generic test for view's remove(Object o) method
  private <T> void testViewRemove(
      Collection<T> view, T elementToRemove, Object keyToRemove, int initialSize) {
    assertTrue(
        view.remove(elementToRemove),
        "Removing existing element via view should return true. Element: " + elementToRemove);
    assertEquals(initialSize - 1, map.size(), "Map size should decrease after view remove");
    assertEquals(initialSize - 1, view.size(), "View size should decrease after view remove");
    assertFalse(map.containsKey(keyToRemove), "Element should be removed from map");
    assertFalse(view.contains(elementToRemove), "Element should be removed from view");

    assertFalse(
        view.remove(nonExistingElementForView(view)),
        "Removing non-existing element via view should return false");
    assertEquals(
        initialSize - 1, map.size(), "Map size should not change after removing non-existing");
  }

  // =====================================================
  // View Tests (Common Logic & Placeholders)
  // =====================================================

  // Generic test for iterator's remove() method
  private <T> void testIteratorRemove(
      Collection<T> view, Object keyToRemove, T elementToRemove, int initialSize) {
    Iterator<T> iterator = view.iterator();
    T foundElement = null;
    while (iterator.hasNext()) {
      T current = iterator.next();
      if (Objects.equals(current, elementToRemove)) {
        foundElement = current;
        iterator.remove();
        break;
      }
    }
    assertNotNull(foundElement, "Element to remove was not found via iterator");

    assertEquals(initialSize - 1, map.size(), "Map size should decrease after iterator remove");
    assertEquals(initialSize - 1, view.size(), "View size should decrease after iterator remove");
    assertFalse(map.containsKey(keyToRemove), "Element should be removed from map (key check)");
    assertFalse(view.contains(elementToRemove), "Element should be removed from view");

    // Test IllegalStateException after remove
    Iterator<T> finalIterator = iterator; // Need final variable for lambda
    assertThrows(
        IllegalStateException.class,
        finalIterator::remove,
        "Calling remove() again should throw IllegalStateException");

    // Test IllegalStateException before next
    iterator = view.iterator(); // New iterator
    Iterator<T> finalIteratorBeforeNext = iterator;
    assertThrows(
        IllegalStateException.class,
        finalIteratorBeforeNext::remove,
        "Calling remove() before next() should throw IllegalStateException");
  }

  // Helper to get a non-existing element of the correct type for a view
  private <T> T nonExistingElementForView(Collection<T> view) {
    if (view instanceof Set) {
      if (view.isEmpty() || view.iterator().next() instanceof Integer) {
        return (T) K_NON_EXIST;
      } else if (view.iterator().next() instanceof String) {
        return (T) V_NON_EXIST;
      } else if (view.iterator().next() instanceof Map.Entry) {
        return (T) new AbstractMap.SimpleImmutableEntry<>(K_NON_EXIST, V_NON_EXIST);
      }
    } else if (view instanceof Collection) {
      return (T) V_NON_EXIST;
    }
    throw new IllegalArgumentException("Unsupported view type for nonExistingElementForView");
  }

  // Helper to get the correct view type from a map instance
  private <T> Collection<T> getViewForMap(
      CustomLinkedHashMap<Integer, String> targetMap, Class<?> viewClass) {
    if (viewClass.getName().contains("KeySetView")) {
      return (Collection<T>) targetMap.keySet();
    } else if (viewClass.getName().contains("ValuesView")) {
      return (Collection<T>) targetMap.values();
    } else if (viewClass.getName().contains("EntrySetView")) {
      return (Collection<T>) targetMap.entrySet();
    }
    throw new IllegalArgumentException("Unsupported view class: " + viewClass);
  }

  // Helper to create a copy of the map for destructive tests
  private CustomLinkedHashMap<Integer, String> createCopy(
      CustomLinkedHashMap<Integer, String> source) {
    CustomLinkedHashMap<Integer, String> copy = new CustomLinkedHashMap<>();
    copy.putAll(source);
    return copy;
  }

  /**
   * Asserts that iterating over the map's entrySet yields the expected keys and values in order.
   * Also checks keySet, values, and direct iterator.
   */
  private void assertIterationOrder(
      CustomLinkedHashMap<Integer, String> mapToTest,
      List<Integer> expectedKeys,
      List<String> expectedValues) {
    assertIterationOrderSpecific(
        mapToTest.keySet(), expectedKeys, "KeySet iteration order mismatch");
    assertIterationOrderSpecific(
        mapToTest.values(), expectedValues, "Values iteration order mismatch");

    List<Map.Entry<Integer, String>> expectedEntries = new ArrayList<>();
    for (int i = 0; i < expectedKeys.size(); i++) {
      expectedEntries.add(
          new AbstractMap.SimpleImmutableEntry<>(expectedKeys.get(i), expectedValues.get(i)));
    }
    assertIterationOrderSpecific(
        mapToTest.entrySet(), expectedEntries, "EntrySet iteration order mismatch");

    // Also check direct iterator (values)
    List<String> directIteratorValues = new ArrayList<>();
    mapToTest.iterator().forEachRemaining(directIteratorValues::add);
    assertIterableEquals(
        expectedValues, directIteratorValues, "Direct iterator (values) order mismatch");
  }

  /** Asserts that iterating over a specific collection yields elements in the expected order. */
  private <T> void assertIterationOrderSpecific(
      Iterable<T> iterable, List<T> expectedOrder, String message) {
    List<T> actualOrder = new ArrayList<>();
    iterable.iterator().forEachRemaining(actualOrder::add);
    assertIterableEquals(expectedOrder, actualOrder, message);
  }

  // Overload for simple assertion without custom message
  private <T> void assertIterationOrderSpecific(Iterable<T> iterable, List<T> expectedOrder) {
    assertIterationOrderSpecific(
        iterable, expectedOrder, "Iteration order mismatch for the specific view/iterable");
  }

  @Nested
  @DisplayName("Basic Map Operations")
  class BasicOperations {

    @Test
    @DisplayName("put() should add new element and return null")
    void put_AddNewElement_ShouldReturnNull() {
      assertNull(map.put(K4, V4), "Putting a new key should return null");
      assertEquals(4, map.size(), "Size should increase");
      assertEquals(V4, map.get(K4), "New value should be retrievable");
      assertEquals(K4, map.lastEntry().getKey(), "New element should be last");
    }

    @Test
    @DisplayName("put() should replace existing element and return old value")
    void put_ReplaceExistingElement_ShouldReturnOldValue() {
      assertEquals(
          V2, map.put(K2, V_REPLACE), "Putting an existing key should return the old value");
      assertEquals(3, map.size(), "Size should remain the same");
      assertEquals(V_REPLACE, map.get(K2), "Value should be updated");
      // Verify order didn't change for the updated element
      List<Integer> keys = new ArrayList<>(map.keySet());
      assertEquals(List.of(K1, K2, K3), keys, "Order should be preserved on update");
    }

    @Test
    @DisplayName("put() into empty map")
    void put_IntoEmptyMap_ShouldWork() {
      assertNull(mapEmpty.put(K1, V1));
      assertEquals(1, mapEmpty.size());
      assertEquals(V1, mapEmpty.get(K1));
      assertEquals(K1, mapEmpty.firstEntry().getKey());
      assertEquals(K1, mapEmpty.lastEntry().getKey());
    }

    @Test
    @DisplayName("put() should throw NullPointerException for null key")
    void put_NullKey_ShouldThrowNPE() {
      assertThrows(
          NullPointerException.class, () -> map.put(null, V1), "Null key should throw NPE");
    }

    @Test
    @DisplayName("put() should throw NullPointerException for null value")
    void put_NullValue_ShouldThrowNPE() {
      assertThrows(
          NullPointerException.class, () -> map.put(K4, null), "Null value should throw NPE");
    }

    @Test
    @DisplayName("get() should return correct value for existing key")
    void get_ExistingKey_ShouldReturnCorrectValue() {
      assertEquals(V1, map.get(K1));
      assertEquals(V2, map.get(K2));
      assertEquals(V3, map.get(K3));
    }

    @Test
    @DisplayName("get() should return null for non-existent key")
    void get_NonExistentKey_ShouldReturnNull() {
      assertNull(map.get(K_NON_EXIST));
      assertNull(mapEmpty.get(K1));
    }

    @Test
    @DisplayName("get() should throw NullPointerException for null key")
    void get_NullKey_ShouldThrowNPE() {
      assertThrows(NullPointerException.class, () -> map.get(null), "Null key should throw NPE");
    }

    @Test
    @DisplayName("remove() should remove existing element and return its value")
    void remove_ExistingKey_ShouldRemoveAndReturnOldValue() {
      assertEquals(V2, map.remove(K2), "Remove should return the value of the removed key");
      assertEquals(2, map.size(), "Size should decrease");
      assertFalse(map.containsKey(K2), "Removed key should not be present");
      assertNull(map.get(K2), "Getting removed key should return null");
      // Check order after removing middle element
      assertIterationOrder(map, List.of(K1, K3), List.of(V1, V3));
      assertEquals(K1, map.firstEntry().getKey());
      assertEquals(K3, map.lastEntry().getKey());
    }

    @Test
    @DisplayName("remove() first element")
    void remove_FirstElement_ShouldUpdateLinks() {
      assertEquals(V1, map.remove(K1));
      assertEquals(2, map.size());
      assertFalse(map.containsKey(K1));
      assertEquals(K2, map.firstEntry().getKey());
      assertEquals(V2, map.firstEntry().getValue());
      assertEquals(K3, map.lastEntry().getKey());
      assertIterationOrder(map, List.of(K2, K3), List.of(V2, V3));
    }

    @Test
    @DisplayName("remove() last element")
    void remove_LastElement_ShouldUpdateLinks() {
      assertEquals(V3, map.remove(K3));
      assertEquals(2, map.size());
      assertFalse(map.containsKey(K3));
      assertEquals(K1, map.firstEntry().getKey());
      assertEquals(K2, map.lastEntry().getKey());
      assertEquals(V2, map.lastEntry().getValue());
      assertIterationOrder(map, List.of(K1, K2), List.of(V1, V2));
    }

    @Test
    @DisplayName("remove() only element")
    void remove_OnlyElement_ShouldMakeMapEmpty() {
      assertEquals(V1, mapSingleElement.remove(K1));
      assertTrue(mapSingleElement.isEmpty());
      assertEquals(0, mapSingleElement.size());
      assertNull(mapSingleElement.firstEntry());
      assertNull(mapSingleElement.lastEntry());
      // Ensure caches are potentially cleared (test by getting views)
      assertTrue(mapSingleElement.isEmpty());
      assertTrue(mapSingleElement.isEmpty());
      assertTrue(mapSingleElement.isEmpty());
    }

    @Test
    @DisplayName("remove() should return null for non-existent key")
    void remove_NonExistentKey_ShouldReturnNull() {
      assertNull(map.remove(K_NON_EXIST), "Remove non-existent key should return null");
      assertEquals(3, map.size(), "Size should not change");
    }

    @Test
    @DisplayName("remove() from empty map")
    void remove_FromEmptyMap_ShouldReturnNull() {
      assertNull(mapEmpty.remove(K1));
      assertTrue(mapEmpty.isEmpty());
    }

    @Test
    @DisplayName("remove() should throw NullPointerException for null key")
    void remove_NullKey_ShouldThrowNPE() {
      assertThrows(NullPointerException.class, () -> map.remove(null), "Null key should throw NPE");
    }

    @Test
    @DisplayName("containsKey() should work correctly")
    void containsKey_ShouldReturnCorrectly() {
      assertTrue(map.containsKey(K1));
      assertTrue(map.containsKey(K2));
      assertTrue(map.containsKey(K3));
      assertFalse(map.containsKey(K4));
      assertFalse(map.containsKey(K_NON_EXIST));
      assertFalse(mapEmpty.containsKey(K1));
    }

    @Test
    @DisplayName("containsKey() should throw NullPointerException for null key")
    void containsKey_NullKey_ShouldThrowNPE() {
      assertThrows(
          NullPointerException.class, () -> map.containsKey(null), "Null key should throw NPE");
    }

    @Test
    @DisplayName("containsValue() should work correctly")
    void containsValue_ShouldReturnCorrectly() {
      assertTrue(map.containsValue(V1));
      assertTrue(map.containsValue(V2));
      assertTrue(map.containsValue(V3));
      assertFalse(map.containsValue(V4));
      assertFalse(map.containsValue(V_NON_EXIST));
      assertFalse(mapEmpty.containsValue(V1));
    }

    @Test
    @DisplayName("containsValue() should throw NullPointerException for null value")
    void containsValue_NullValue_ShouldThrowNPE() {
      assertThrows(
          NullPointerException.class, () -> map.containsValue(null), "Null value should throw NPE");
    }

    @Test
    @DisplayName("size() should return correct size")
    void size_ShouldReturnCorrectValue() {
      assertEquals(3, map.size());
      assertEquals(0, mapEmpty.size());
      assertEquals(1, mapSingleElement.size());
      map.put(K4, V4);
      assertEquals(4, map.size());
      map.remove(K1);
      assertEquals(3, map.size());
    }

    @Test
    @DisplayName("isEmpty() should return correct state")
    void isEmpty_ShouldReturnCorrectValue() {
      assertFalse(map.isEmpty());
      assertTrue(mapEmpty.isEmpty());
      assertFalse(mapSingleElement.isEmpty());
      map.clear();
      assertTrue(map.isEmpty());
    }

    @Test
    @DisplayName("clear() should remove all elements")
    void clear_ShouldRemoveAllElements() {
      SequencedSet<Integer> ks = map.keySet();
      SequencedCollection<String> vs = map.values();
      SequencedSet<Map.Entry<Integer, String>> es = map.entrySet();
      SequencedMap<Integer, String> rm = map.reversed();

      assertFalse(map.isEmpty());
      map.clear();

      assertTrue(map.isEmpty(), "Map should be empty after clear");
      assertEquals(0, map.size(), "Size should be 0 after clear");
      assertNull(map.firstEntry(), "First entry should be null after clear");
      assertNull(map.lastEntry(), "Last entry should be null after clear");
      assertFalse(map.containsKey(K1), "Map should not contain keys after clear");
      assertFalse(map.containsValue(V1), "Map should not contain values after clear");

      // Check views obtained BEFORE clear are now empty
      assertTrue(ks.isEmpty(), "Cached keySet should be empty after clear");
      assertTrue(vs.isEmpty(), "Cached valuesView should be empty after clear");
      assertTrue(es.isEmpty(), "Cached entrySet should be empty after clear");
      assertTrue(rm.isEmpty(), "Cached reversedMap should be empty after clear");

      // Check views obtained AFTER clear are also empty
      assertTrue(map.isEmpty());
      assertTrue(map.isEmpty());
      assertTrue(map.isEmpty());
      assertTrue(map.reversed().isEmpty());
    }

    @Test
    @DisplayName("putAll() should add all elements using putLast semantics")
    void putAll_ShouldAddAllElements() {
      // Use LinkedHashMap for predictable iteration order for source
      Map<Integer, String> sourceMap = new LinkedHashMap<>();
      sourceMap.put(K4, V4);
      sourceMap.put(K1, V_REPLACE);

      map.putAll(sourceMap);

      assertEquals(4, map.size(), "Size should be 4 after putAll");
      assertEquals(V_REPLACE, map.get(K1), "Value for K1 should be updated");
      assertEquals(V2, map.get(K2));
      assertEquals(V3, map.get(K3));
      assertEquals(V4, map.get(K4), "New element K4 should be added");
      // putAll calls putLast, K1 is updated in place, K4 added last.
      assertIterationOrder(map, List.of(K1, K2, K3, K4), List.of(V_REPLACE, V2, V3, V4));
    }

    @Test
    @DisplayName("putAll() into empty map")
    void putAll_IntoEmptyMap() {
      // Use LinkedHashMap for predictable iteration order
      Map<Integer, String> sourceMap = new LinkedHashMap<>();
      sourceMap.put(K1, V1);
      sourceMap.put(K2, V2);

      mapEmpty.putAll(sourceMap);
      assertEquals(2, mapEmpty.size());
      assertEquals(V1, mapEmpty.get(K1));
      assertEquals(V2, mapEmpty.get(K2));
      // Order should reflect insertion order via putLast
      assertIterationOrder(mapEmpty, List.of(K1, K2), List.of(V1, V2));
    }

    @Test
    @DisplayName("putAll() with empty map")
    void putAll_WithEmptyMap() {
      map.putAll(Collections.emptyMap());
      assertEquals(3, map.size()); // Size should not change
      assertIterationOrder(
          map, List.of(K1, K2, K3), List.of(V1, V2, V3)); // Order should not change
    }

    @Test
    @DisplayName("putAll() should throw NullPointerException for null map")
    void putAll_NullMap_ShouldThrowNPE() {
      assertThrows(NullPointerException.class, () -> map.putAll(null));
    }

    @Test
    @DisplayName("remove(key, value) should remove only if key and value match")
    void remove_KeyValue_Matching() {
      assertTrue(map.remove(K2, V2), "Should return true when key and value match");
      assertEquals(2, map.size());
      assertFalse(map.containsKey(K2));
    }

    @Test
    @DisplayName("remove(key, value) should not remove if value doesn't match")
    void remove_KeyValue_ValueMismatch() {
      assertFalse(map.remove(K2, "Wrong Value"), "Should return false when value mismatches");
      assertEquals(3, map.size());
      assertTrue(map.containsKey(K2));
      assertEquals(V2, map.get(K2));
    }

    @Test
    @DisplayName("remove(key, value) should not remove if key doesn't exist")
    void remove_KeyValue_KeyNonExistent() {
      assertFalse(map.remove(K_NON_EXIST, V1), "Should return false when key does not exist");
      assertEquals(3, map.size());
    }

    @Test
    @DisplayName("remove(key, value) should handle non-matching null value")
    void remove_KeyValue_MapHasValueTargetIsNull() {
      // Try removing key K1 with null value when V1 is stored
      assertFalse(map.remove(K1, null));
      assertEquals(3, map.size());
      assertTrue(map.containsKey(K1));
    }

    @Test
    @DisplayName("remove(key, value) should throw NullPointerException for null key")
    void remove_KeyValue_NullArgs() {
      assertThrows(NullPointerException.class, () -> map.remove(null, V1));
    }
  }

  // =====================================================
  // SequencedMap Operations Tests
  // =====================================================
  @Nested
  @DisplayName("SequencedMap Operations")
  class SequencedOperations {

    @Test
    @DisplayName("putFirst() should add to beginning if key is new")
    void putFirst_NewKey_ShouldAddToBeginning() {
      assertNull(map.putFirst(K4, V4));
      assertEquals(4, map.size());
      assertEquals(V4, map.get(K4));
      assertEquals(K4, map.firstEntry().getKey());
      assertEquals(V4, map.firstEntry().getValue());
      assertEquals(K3, map.lastEntry().getKey());
      assertIterationOrder(map, List.of(K4, K1, K2, K3), List.of(V4, V1, V2, V3));
    }

    @Test
    @DisplayName("putFirst() should update value and return old if key exists, maintaining order")
    void putFirst_ExistingKey_ShouldUpdateAndReturnOld() {
      assertEquals(V2, map.putFirst(K2, V_REPLACE));
      assertEquals(3, map.size());
      assertEquals(V_REPLACE, map.get(K2));
      // Order MUST NOT change
      assertIterationOrder(map, List.of(K1, K2, K3), List.of(V1, V_REPLACE, V3));
      assertEquals(K1, map.firstEntry().getKey());
      assertEquals(K3, map.lastEntry().getKey());
    }

    @Test
    @DisplayName("putFirst() into empty map")
    void putFirst_EmptyMap() {
      assertNull(mapEmpty.putFirst(K1, V1));
      assertEquals(1, mapEmpty.size());
      assertEquals(K1, mapEmpty.firstEntry().getKey());
      assertEquals(K1, mapEmpty.lastEntry().getKey());
      assertEquals(V1, mapEmpty.get(K1));
    }

    @Test
    @DisplayName("putLast() should add to end if key is new")
    void putLast_NewKey_ShouldAddToEnd() {
      assertNull(map.putLast(K4, V4));
      assertEquals(4, map.size());
      assertEquals(V4, map.get(K4));
      assertEquals(K1, map.firstEntry().getKey());
      assertEquals(K4, map.lastEntry().getKey());
      assertEquals(V4, map.lastEntry().getValue());
      assertIterationOrder(map, List.of(K1, K2, K3, K4), List.of(V1, V2, V3, V4));
    }

    @Test
    @DisplayName("putLast() should update value and return old if key exists, maintaining order")
    void putLast_ExistingKey_ShouldUpdateAndReturnOld() {
      assertEquals(V2, map.putLast(K2, V_REPLACE));
      assertEquals(3, map.size());
      assertEquals(V_REPLACE, map.get(K2));
      // Order MUST NOT change
      assertIterationOrder(map, List.of(K1, K2, K3), List.of(V1, V_REPLACE, V3));
      assertEquals(K1, map.firstEntry().getKey());
      assertEquals(K3, map.lastEntry().getKey());
    }

    @Test
    @DisplayName("putLast() into empty map")
    void putLast_EmptyMap() {
      assertNull(mapEmpty.putLast(K1, V1));
      assertEquals(1, mapEmpty.size());
      assertEquals(K1, mapEmpty.firstEntry().getKey());
      assertEquals(K1, mapEmpty.lastEntry().getKey());
      assertEquals(V1, mapEmpty.get(K1));
    }

    @Test
    @DisplayName("firstEntry() should return first element or null")
    void firstEntry_ShouldReturnCorrectEntry() {
      Map.Entry<Integer, String> first = map.firstEntry();
      assertNotNull(first);
      assertEquals(K1, first.getKey());
      assertEquals(V1, first.getValue());

      assertNull(mapEmpty.firstEntry());
    }

    @Test
    @DisplayName("lastEntry() should return last element or null")
    void lastEntry_ShouldReturnCorrectEntry() {
      Map.Entry<Integer, String> last = map.lastEntry();
      assertNotNull(last);
      assertEquals(K3, last.getKey());
      assertEquals(V3, last.getValue());

      assertNull(mapEmpty.lastEntry());
    }

    @Test
    @DisplayName("pollFirstEntry() should remove and return first element or null")
    void pollFirstEntry_ShouldRemoveAndReturnFirst() {
      Map.Entry<Integer, String> expectedFirst = map.firstEntry();
      Map.Entry<Integer, String> polled = map.pollFirstEntry();
      assertNotNull(polled);
      assertEquals(expectedFirst, polled, "Polled entry should match first entry");
      assertEquals(K1, polled.getKey());
      assertEquals(V1, polled.getValue());
      assertEquals(2, map.size());
      assertFalse(map.containsKey(K1));
      assertEquals(K2, map.firstEntry().getKey());
      assertIterationOrder(map, List.of(K2, K3), List.of(V2, V3));

      // Poll next
      Map.Entry<Integer, String> polled2 = map.pollFirstEntry();
      assertEquals(K2, polled2.getKey());
      assertEquals(1, map.size());

      // Poll last remaining
      Map.Entry<Integer, String> lastPolled = map.pollFirstEntry();
      assertEquals(K3, lastPolled.getKey());
      assertTrue(map.isEmpty());
      assertNull(map.pollFirstEntry());
    }

    @Test
    @DisplayName("pollFirstEntry() on empty map")
    void pollFirstEntry_EmptyMap() {
      assertNull(mapEmpty.pollFirstEntry());
    }

    @Test
    @DisplayName("pollLastEntry() should remove and return last element or null")
    void pollLastEntry_ShouldRemoveAndReturnLast() {
      Map.Entry<Integer, String> expectedLast = map.lastEntry();
      Map.Entry<Integer, String> polled = map.pollLastEntry();
      assertNotNull(polled);
      assertEquals(expectedLast, polled, "Polled entry should match last entry");
      assertEquals(K3, polled.getKey());
      assertEquals(V3, polled.getValue());
      assertEquals(2, map.size());
      assertFalse(map.containsKey(K3));
      assertEquals(K2, map.lastEntry().getKey());
      assertIterationOrder(map, List.of(K1, K2), List.of(V1, V2));

      // Poll next
      Map.Entry<Integer, String> polled2 = map.pollLastEntry();
      assertEquals(K2, polled2.getKey());
      assertEquals(1, map.size());

      // Poll last remaining
      Map.Entry<Integer, String> lastPolled = map.pollLastEntry();
      assertEquals(K1, lastPolled.getKey());
      assertTrue(map.isEmpty());
      assertNull(map.pollLastEntry());
    }

    @Test
    @DisplayName("pollLastEntry() on empty map")
    void pollLastEntry_EmptyMap() {
      assertNull(mapEmpty.pollLastEntry());
    }
  }

  // =====================================================
  // KeySetView Tests
  // =====================================================
  @Nested
  @DisplayName("KeySet View Tests")
  class KeySetViewTests {
    private SequencedSet<Integer> keySet;

    @BeforeEach
    void setUpView() {
      keySet = map.keySet();
    }

    @Test
    @DisplayName("Basic KeySet Operations")
    void keySet_BasicOperations() {
      testViewBasics(keySet, K2, K_NON_EXIST, 3);
    }

    @Test
    @DisplayName("KeySet remove(Object o)")
    void keySet_Remove() {
      testViewRemove(keySet, K2, K2, 3);
    }

    @Test
    @DisplayName("KeySet iterator remove()")
    void keySet_IteratorRemove() {
      testIteratorRemove(keySet, K2, K2, 3);
    }

    @Test
    @DisplayName("KeySet Iteration Order (Forward)")
    void keySet_IterationOrderForward() {
      assertIterationOrderSpecific(keySet, List.of(K1, K2, K3));
    }

    @Test
    @DisplayName("KeySet getFirst() / getLast()")
    void keySet_GetFirstLast() {
      assertEquals(K1, keySet.getFirst());
      assertEquals(K3, keySet.getLast());
      assertThrows(NoSuchElementException.class, mapEmpty.keySet()::getFirst);
      assertThrows(NoSuchElementException.class, mapEmpty.keySet()::getLast);
    }

    @Test
    @DisplayName("Map modification should reflect in KeySet")
    void mapModification_ReflectsInKeySet() {
      assertTrue(keySet.contains(K1));
      map.remove(K1);
      assertFalse(keySet.contains(K1), "KeySet should not contain removed key");
      assertEquals(2, keySet.size(), "KeySet size should decrease");

      map.put(K4, V4);
      assertTrue(keySet.contains(K4), "KeySet should contain newly added key");
      assertEquals(3, keySet.size(), "KeySet size should increase");
    }

    @Test
    @DisplayName("KeySet modification should reflect in Map")
    void keySetModification_ReflectsInMap() {
      assertTrue(map.containsKey(K2));
      keySet.remove(K2);
      assertFalse(map.containsKey(K2), "Map should not contain key removed via keySet");
      assertEquals(2, map.size(), "Map size should decrease");

      keySet.clear();
      assertTrue(map.isEmpty(), "Map should be empty after keySet clear");
    }

    @Nested
    @DisplayName("Reversed KeySet View")
    class ReversedKeySetTests {
      private SequencedSet<Integer> reversedKeySet;

      @BeforeEach
      void setUpReversedView() {
        reversedKeySet = keySet.reversed();
        assertNotNull(reversedKeySet, "Reversed key set should not be null");
      }

      @Test
      @DisplayName("Basic Reversed KeySet Operations")
      void reversedKeySet_BasicOperations() {
        testViewBasics(reversedKeySet, K2, K_NON_EXIST, 3);
      }

      @Test
      @DisplayName("Reversed KeySet remove(Object o)")
      void reversedKeySet_Remove() {
        testViewRemove(reversedKeySet, K2, K2, 3);
      }

      @Test
      @DisplayName("Reversed KeySet iterator remove()")
      void reversedKeySet_IteratorRemove() {
        // Test removing K2 via reversed iterator
        testIteratorRemove(reversedKeySet, K2, K2, 3);
        // Check remaining order
        assertIterationOrderSpecific(reversedKeySet, List.of(K3, K1));
      }

      @Test
      @DisplayName("Reversed KeySet Iteration Order")
      void reversedKeySet_IterationOrder() {
        assertIterationOrderSpecific(reversedKeySet, List.of(K3, K2, K1));
      }

      @Test
      @DisplayName("Reversed KeySet getFirst() / getLast()")
      void reversedKeySet_GetFirstLast() {
        assertEquals(K3, reversedKeySet.getFirst());
        assertEquals(K1, reversedKeySet.getLast());
        assertThrows(NoSuchElementException.class, mapEmpty.keySet().reversed()::getFirst);
        assertThrows(NoSuchElementException.class, mapEmpty.keySet().reversed()::getLast);
      }

      @Test
      @DisplayName("reversed().reversed() should return original view instance")
      void reversedReversed_ShouldBeOriginal() {
        // Check if the same instance is returned (if cached)
        assertSame(
            keySet,
            reversedKeySet.reversed(),
            "reversed().reversed() should be the same instance if cached");
        // Also check equality
        assertEquals(keySet, reversedKeySet.reversed());
      }
    }
  }

  // =====================================================
  // ValuesView Tests
  // =====================================================
  @Nested
  @DisplayName("Values Collection View Tests")
  class ValuesViewTests {
    private SequencedCollection<String> valuesView;

    @BeforeEach
    void setUpView() {
      valuesView = map.values();
    }

    @Test
    @DisplayName("Basic Values Operations")
    void values_BasicOperations() {
      // Pass V_NON_EXIST as non-existing element
      testViewBasics(valuesView, V2, V_NON_EXIST, 3);
    }

    @Test
    @DisplayName("Values iterator remove()")
    void values_IteratorRemove() {
      // Remove V2 (associated with K2)
      testIteratorRemove(valuesView, K2, V2, 3);
    }

    @Test
    @DisplayName("Values Iteration Order (Forward)")
    void values_IterationOrderForward() {
      assertIterationOrderSpecific(valuesView, List.of(V1, V2, V3));
    }

    @Test
    @DisplayName("Values getFirst() / getLast()")
    void values_GetFirstLast() {
      assertEquals(V1, valuesView.getFirst());
      assertEquals(V3, valuesView.getLast());
      assertThrows(NoSuchElementException.class, mapEmpty.values()::getFirst);
      assertThrows(NoSuchElementException.class, mapEmpty.values()::getLast);
    }

    @Test
    @DisplayName("Map modification should reflect in Values")
    void mapModification_ReflectsInValues() {
      assertTrue(valuesView.contains(V1));
      map.remove(K1);
      assertFalse(valuesView.contains(V1), "Values view should not contain value of removed key");
      assertEquals(2, valuesView.size());

      map.put(K4, V4);
      assertTrue(valuesView.contains(V4), "Values view should contain newly added value");
      assertEquals(3, valuesView.size());

      map.put(K2, V_REPLACE);
      assertTrue(valuesView.contains(V_REPLACE));
      assertFalse(valuesView.contains(V2));
      assertEquals(3, valuesView.size());
    }

    @Test
    @DisplayName("Values modification (via iterator remove) should reflect in Map")
    void valuesModification_ReflectsInMap() {
      assertTrue(map.containsValue(V2));
      testIteratorRemove(valuesView, K2, V2, 3);
      assertFalse(map.containsValue(V2));
      assertFalse(map.containsKey(K2));

      valuesView.clear();
      assertTrue(map.isEmpty());
    }

    // Tests for remove(Object o) in ValuesView - typically unsupported or less defined
    @Test
    @DisplayName("Values remove(Object o) - Check if supported/correct")
    void values_RemoveObject() {
      // AbstractCollection remove iterates and uses iterator.remove()
      // It should remove the *first* occurrence
      map.put(K4, V1);
      assertIterationOrderSpecific(valuesView, List.of(V1, V2, V3, V1));
      assertTrue(valuesView.remove(V1), "remove(V1) should return true");
      assertEquals(3, valuesView.size());
      assertEquals(3, map.size());
      assertFalse(map.containsKey(K1), "First entry with V1 (K1) should be removed");
      assertTrue(map.containsKey(K4), "Second entry with V1 (K4) should remain");
      assertIterationOrderSpecific(valuesView, List.of(V2, V3, V1));

      assertFalse(
          valuesView.remove(V_NON_EXIST), "Removing non-existing value should return false");
    }

    @Nested
    @DisplayName("Reversed Values Collection View")
    class ReversedValuesTests {
      private SequencedCollection<String> reversedValuesView;

      @BeforeEach
      void setUpReversedView() {
        reversedValuesView = valuesView.reversed();
        assertNotNull(reversedValuesView, "Reversed values view should not be null");
      }

      @Test
      @DisplayName("Basic Reversed Values Operations")
      void reversedValues_BasicOperations() {
        testViewBasics(reversedValuesView, V2, V_NON_EXIST, 3);
      }

      @Test
      @DisplayName("Reversed Values iterator remove()")
      void reversedValues_IteratorRemove() {
        // Remove V2 (associated with K2) via reversed iterator
        testIteratorRemove(reversedValuesView, K2, V2, 3);
        // Check remaining order
        assertIterationOrderSpecific(reversedValuesView, List.of(V3, V1));
      }

      @Test
      @DisplayName("Reversed Values Iteration Order")
      void reversedValues_IterationOrder() {
        assertIterationOrderSpecific(reversedValuesView, List.of(V3, V2, V1));
      }

      @Test
      @DisplayName("Reversed Values getFirst() / getLast()")
      void reversedValues_GetFirstLast() {
        assertEquals(V3, reversedValuesView.getFirst());
        assertEquals(V1, reversedValuesView.getLast());
        assertThrows(NoSuchElementException.class, mapEmpty.values().reversed()::getFirst);
        assertThrows(NoSuchElementException.class, mapEmpty.values().reversed()::getLast);
      }

      @Test
      @DisplayName("reversed().reversed() should return original view instance")
      void reversedReversed_ShouldBeOriginal() {
        assertSame(
            valuesView,
            reversedValuesView.reversed(),
            "reversed().reversed() should be the same instance if cached");
        // Also check equality (AbstractCollection equals is based on iterator content/order)
        assertEquals(valuesView.stream().toList(), reversedValuesView.reversed().stream().toList());
      }
    }
  }

  // =====================================================
  // EntrySetView Tests
  // =====================================================
  @Nested
  @DisplayName("EntrySet View Tests")
  class EntrySetViewTests {
    private SequencedSet<Map.Entry<Integer, String>> entrySet;
    private Map.Entry<Integer, String> entry1;
    private Map.Entry<Integer, String> entry2;
    private Map.Entry<Integer, String> entry3;
    private Map.Entry<Integer, String> nonExistingEntryKey;
    private Map.Entry<Integer, String> nonExistingEntryValue;
    private Map.Entry<Integer, String> nonExistingEntryBoth;

    @BeforeEach
    void setUpView() {
      entrySet = map.entrySet();
      // Use immutable entries for comparison
      entry1 = new AbstractMap.SimpleImmutableEntry<>(K1, V1);
      entry2 = new AbstractMap.SimpleImmutableEntry<>(K2, V2);
      entry3 = new AbstractMap.SimpleImmutableEntry<>(K3, V3);
      nonExistingEntryKey = new AbstractMap.SimpleImmutableEntry<>(K_NON_EXIST, V1);
      nonExistingEntryValue = new AbstractMap.SimpleImmutableEntry<>(K1, V_NON_EXIST);
      nonExistingEntryBoth = new AbstractMap.SimpleImmutableEntry<>(K_NON_EXIST, V_NON_EXIST);
    }

    @Test
    @DisplayName("Basic EntrySet Operations")
    void entrySet_BasicOperations() {
      // Pass nonExistingEntryBoth as non-existing
      testViewBasics(entrySet, entry2, nonExistingEntryBoth, 3);
      // Specific contains checks for EntrySet
      assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>(K1, V1)));
      assertFalse(entrySet.contains(nonExistingEntryKey));
      assertFalse(entrySet.contains(nonExistingEntryValue));
      assertFalse(entrySet.contains(new Object()));
    }

    @Test
    @DisplayName("EntrySet remove(Object o)")
    void entrySet_Remove() {
      testViewRemove(entrySet, entry2, K2, 3);
      assertFalse(entrySet.remove(nonExistingEntryKey), "Removing non-existing key entry");
      assertFalse(entrySet.remove(nonExistingEntryValue), "Removing non-existing value entry");
      assertFalse(entrySet.remove(nonExistingEntryBoth), "Removing non-existing entry");
      assertFalse(entrySet.remove(new Object()), "Removing wrong type");
    }

    @Test
    @DisplayName("EntrySet iterator remove()")
    void entrySet_IteratorRemove() {
      // Remove entry2 (K2, V2)
      testIteratorRemove(entrySet, K2, entry2, 3);
      // Check remaining order
      assertIterationOrderSpecific(entrySet, List.of(entry1, entry3));
    }

    @Test
    @DisplayName("EntrySet Iteration Order (Forward)")
    void entrySet_IterationOrderForward() {
      assertIterationOrderSpecific(entrySet, List.of(entry1, entry2, entry3));
    }

    @Test
    @DisplayName("EntrySet getFirst() / getLast()")
    void entrySet_GetFirstLast() {
      assertEquals(entry1, entrySet.getFirst());
      assertEquals(entry3, entrySet.getLast());
      assertThrows(NoSuchElementException.class, mapEmpty.entrySet()::getFirst);
      assertThrows(NoSuchElementException.class, mapEmpty.entrySet()::getLast);
    }

    @Test
    @DisplayName("Map modification should reflect in EntrySet")
    void mapModification_ReflectsInEntrySet() {
      assertTrue(entrySet.contains(entry1));
      map.remove(K1);
      assertFalse(entrySet.contains(entry1), "EntrySet should not contain entry of removed key");
      assertEquals(2, entrySet.size());

      map.put(K4, V4);
      Map.Entry<Integer, String> entry4 = new AbstractMap.SimpleImmutableEntry<>(K4, V4);
      assertTrue(entrySet.contains(entry4), "EntrySet should contain newly added entry");
      assertEquals(3, entrySet.size());

      map.put(K2, V_REPLACE);
      Map.Entry<Integer, String> entry2Updated =
          new AbstractMap.SimpleImmutableEntry<>(K2, V_REPLACE);
      assertTrue(entrySet.contains(entry2Updated));
      assertFalse(entrySet.contains(entry2));
      assertEquals(3, entrySet.size());
    }

    @Test
    @DisplayName("EntrySet modification should reflect in Map")
    void entrySetModification_ReflectsInMap() {
      assertTrue(map.containsKey(K2));
      entrySet.remove(entry2);
      assertFalse(map.containsKey(K2), "Map should not contain key removed via entrySet");
      assertEquals(2, map.size());

      entrySet.clear();
      assertTrue(map.isEmpty(), "Map should be empty after entrySet clear");
    }

    // setValue() is not supported by the immutable entries returned by the current iterator
    @Test
    @DisplayName("EntrySet iterator setValue() should throw UnsupportedOperationException")
    void entrySet_IteratorSetValue() {
      Iterator<Map.Entry<Integer, String>> it = entrySet.iterator();
      assertTrue(it.hasNext());
      Map.Entry<Integer, String> first = it.next();
      assertThrows(UnsupportedOperationException.class, () -> first.setValue(V_REPLACE));
      assertEquals(V1, map.get(K1));
    }

    @Nested
    @DisplayName("Reversed EntrySet View")
    class ReversedEntrySetTests {
      private SequencedSet<Map.Entry<Integer, String>> reversedEntrySet;

      @BeforeEach
      void setUpReversedView() {
        reversedEntrySet = entrySet.reversed();
        assertNotNull(reversedEntrySet, "Reversed entry set should not be null");
      }

      @Test
      @DisplayName("Basic Reversed EntrySet Operations")
      void reversedEntrySet_BasicOperations() {
        testViewBasics(reversedEntrySet, entry2, nonExistingEntryBoth, 3);
      }

      @Test
      @DisplayName("Reversed EntrySet remove(Object o)")
      void reversedEntrySet_Remove() {
        testViewRemove(reversedEntrySet, entry2, K2, 3);
      }

      @Test
      @DisplayName("Reversed EntrySet iterator remove()")
      void reversedEntrySet_IteratorRemove() {
        // Remove entry2 (K2, V2) via reversed iterator
        testIteratorRemove(reversedEntrySet, K2, entry2, 3);
        // Check remaining order
        assertIterationOrderSpecific(reversedEntrySet, List.of(entry3, entry1));
      }

      @Test
      @DisplayName("Reversed EntrySet Iteration Order")
      void reversedEntrySet_IterationOrder() {
        assertIterationOrderSpecific(reversedEntrySet, List.of(entry3, entry2, entry1));
      }

      @Test
      @DisplayName("Reversed EntrySet getFirst() / getLast()")
      void reversedEntrySet_GetFirstLast() {
        assertEquals(entry3, reversedEntrySet.getFirst());
        assertEquals(entry1, reversedEntrySet.getLast());
        assertThrows(NoSuchElementException.class, mapEmpty.entrySet().reversed()::getFirst);
        assertThrows(NoSuchElementException.class, mapEmpty.entrySet().reversed()::getLast);
      }

      @Test
      @DisplayName("reversed().reversed() should return original view instance")
      void reversedReversed_ShouldBeOriginal() {
        assertSame(
            entrySet,
            reversedEntrySet.reversed(),
            "reversed().reversed() should be the same instance if cached");
        assertEquals(entrySet, reversedEntrySet.reversed());
      }
    }
  }

  // =====================================================
  // Reversed Map Tests
  // =====================================================
  @Nested
  @DisplayName("Reversed Map View Tests")
  class ReversedMapTests {
    private SequencedMap<Integer, String> reversedMap;

    @BeforeEach
    void setUpReversedMap() {
      reversedMap = map.reversed();
      assertNotNull(reversedMap, "Reversed map should not be null");
    }

    @Test
    @DisplayName("Basic Reversed Map Operations")
    void reversed_BasicOperations() {
      assertEquals(3, reversedMap.size());
      assertFalse(reversedMap.isEmpty());
      assertTrue(reversedMap.containsKey(K2));
      assertTrue(reversedMap.containsValue(V2));
      assertEquals(V1, reversedMap.get(K1));
      assertNull(reversedMap.get(K_NON_EXIST));
    }

    @Test
    @DisplayName("reversed().reversed() should return original map instance")
    void reversedReversed_ShouldBeOriginal() {
      assertSame(
          map,
          reversedMap.reversed(),
          "reversed().reversed() should be the same instance if cached");
      assertEquals(map, reversedMap.reversed());
    }

    @Test
    @DisplayName("firstEntry() / lastEntry() on Reversed Map")
    void reversed_FirstLastEntry() {
      assertEquals(map.lastEntry(), reversedMap.firstEntry());
      assertEquals(map.firstEntry(), reversedMap.lastEntry());
      assertNull(mapEmpty.reversed().firstEntry());
      assertNull(mapEmpty.reversed().lastEntry());
    }

    @Test
    @DisplayName("pollFirstEntry() / pollLastEntry() on Reversed Map")
    void reversed_PollFirstLastEntry() {
      Map.Entry<Integer, String> originalLast = map.lastEntry();

      Map.Entry<Integer, String> polledFirstReversed = reversedMap.pollFirstEntry();
      assertEquals(originalLast, polledFirstReversed);
      assertEquals(2, reversedMap.size());
      assertEquals(2, map.size());
      assertFalse(reversedMap.containsKey(K3));
      assertEquals(map.lastEntry(), reversedMap.firstEntry());

      Map.Entry<Integer, String> polledLastReversed = reversedMap.pollLastEntry();
      assertEquals(K1, polledLastReversed.getKey());
      assertEquals(V1, polledLastReversed.getValue());
      assertEquals(1, reversedMap.size());
      assertEquals(1, map.size());
      assertFalse(reversedMap.containsKey(K1));
      assertEquals(map.firstEntry(), reversedMap.lastEntry());
      assertEquals(map.lastEntry(), reversedMap.firstEntry());
    }

    @Test
    @DisplayName("putFirst() on Reversed Map adds to original end")
    void reversed_PutFirst() {
      assertNull(reversedMap.putFirst(K4, V4));
      assertEquals(4, map.size());
      assertEquals(K4, map.lastEntry().getKey());
      assertEquals(K1, map.firstEntry().getKey());
      assertEquals(K1, reversedMap.lastEntry().getKey());
      assertEquals(K4, reversedMap.firstEntry().getKey());
      assertIterationOrder(map, List.of(K1, K2, K3, K4), List.of(V1, V2, V3, V4));
    }

    @Test
    @DisplayName("putLast() on Reversed Map adds to original start")
    void reversed_PutLast() {
      assertNull(reversedMap.putLast(K4, V4));
      assertEquals(4, map.size());
      assertEquals(K4, map.firstEntry().getKey());
      assertEquals(K3, map.lastEntry().getKey());
      assertEquals(K3, reversedMap.firstEntry().getKey());
      assertEquals(K4, reversedMap.lastEntry().getKey());
      assertIterationOrder(map, List.of(K4, K1, K2, K3), List.of(V4, V1, V2, V3));
    }

    @Test
    @DisplayName("put() on Reversed Map updates or adds to original start")
    void reversed_Put() {
      // Test add new (putLast on reversed -> putFirst on original)
      assertNull(reversedMap.put(K4, V4));
      assertEquals(4, map.size());
      assertEquals(K4, map.firstEntry().getKey());
      assertEquals(K4, reversedMap.lastEntry().getKey());
      assertIterationOrder(map, List.of(K4, K1, K2, K3), List.of(V4, V1, V2, V3));

      // Test update existing
      assertEquals(V2, reversedMap.put(K2, V_REPLACE));
      assertEquals(4, map.size());
      assertEquals(V_REPLACE, map.get(K2));
      // Order should not change for updated element
      assertIterationOrder(map, List.of(K4, K1, K2, K3), List.of(V4, V1, V_REPLACE, V3));
    }

    @Test
    @DisplayName("putAll() on Reversed Map")
    void reversed_PutAll() {
      Map<Integer, String> source = new LinkedHashMap<>();
      source.put(K4, V4);
      source.put(K1, V_REPLACE);

      reversedMap.putAll(source);

      assertEquals(4, map.size());
      assertEquals(V_REPLACE, map.get(K1));
      assertEquals(V4, map.get(K4));
      assertIterationOrder(map, List.of(K4, K1, K2, K3), List.of(V4, V_REPLACE, V2, V3));
    }

    @Test
    @DisplayName("Views obtained from Reversed Map iterate in reverse")
    void reversed_Views() {
      SequencedSet<Integer> rk = (SequencedSet<Integer>) reversedMap.keySet();
      SequencedCollection<String> rv = (SequencedCollection<String>) reversedMap.values();
      SequencedSet<Map.Entry<Integer, String>> re =
          (SequencedSet<Map.Entry<Integer, String>>) reversedMap.entrySet();

      // Check iteration order
      assertIterationOrderSpecific(rk, List.of(K3, K2, K1));
      assertIterationOrderSpecific(rv, List.of(V3, V2, V1));
      // Create expected reversed entries
      List<Map.Entry<Integer, String>> expectedEntries =
          List.of(
              new AbstractMap.SimpleImmutableEntry<>(K3, V3),
              new AbstractMap.SimpleImmutableEntry<>(K2, V2),
              new AbstractMap.SimpleImmutableEntry<>(K1, V1));
      assertIterationOrderSpecific(re, expectedEntries);

      // Check first/last
      assertEquals(K3, rk.getFirst());
      assertEquals(K1, rk.getLast());
      assertEquals(V3, rv.getFirst());
      assertEquals(V1, rv.getLast());
      assertEquals(expectedEntries.get(0), re.getFirst());
      assertEquals(expectedEntries.get(2), re.getLast());

      // Check modification via reversed view's view
      rk.remove(K2);
      assertEquals(2, map.size());
      assertFalse(map.containsKey(K2));
      assertIterationOrderSpecific(rk, List.of(K3, K1));
    }

    @Test
    @DisplayName("clear() via Reversed Map")
    void reversed_Clear() {
      assertFalse(map.isEmpty());
      reversedMap.clear();
      assertTrue(map.isEmpty());
      assertTrue(reversedMap.isEmpty());
    }

    @Test
    @DisplayName("equals() and hashCode() on Reversed Map")
    void reversed_EqualsHashCode() {
      CustomLinkedHashMap<Integer, String> map2 = new CustomLinkedHashMap<>();
      map2.put(K1, V1);
      map2.put(K2, V2);
      map2.put(K3, V3);

      assertEquals(reversedMap, map, "ReversedMapView equals check needs adjustment for type");
      assertEquals(map, map2);
      assertEquals(
          map.hashCode(),
          reversedMap.hashCode(),
          "Reversed map hashcode should equal original map hashcode");
    }
  }

  // =====================================================
  // Iterable Tests
  // =====================================================
  @Nested
  @DisplayName("Iterable<V> Tests")
  class IterableTests {
    @Test
    @DisplayName("Direct iterator() iterates values in order")
    void iterator_IteratesValuesInOrder() {
      List<String> iteratedValues = new ArrayList<>();
      map.iterator().forEachRemaining(iteratedValues::add);
      assertEquals(List.of(V1, V2, V3), iteratedValues);
    }

    @Test
    @DisplayName("Direct iterator() remove() works")
    void iterator_RemoveWorks() {
      Iterator<String> it = map.iterator();
      assertTrue(it.hasNext());
      assertEquals(V1, it.next());
      assertTrue(it.hasNext());
      assertEquals(V2, it.next());
      it.remove();

      assertEquals(2, map.size());
      assertFalse(map.containsKey(K2));
      assertFalse(map.containsValue(V2));
      assertIterationOrder(map, List.of(K1, K3), List.of(V1, V3));

      // Test exceptions
      assertThrows(IllegalStateException.class, it::remove);
      it = map.iterator();
      assertThrows(IllegalStateException.class, it::remove);
    }

    @Test
    @DisplayName("Direct iterator() on empty map")
    void iterator_EmptyMap() {
      Iterator<String> it = mapEmpty.iterator();
      assertFalse(it.hasNext());
      assertThrows(NoSuchElementException.class, it::next);
    }
  }

  // =====================================================
  // Comparable Tests
  // =====================================================
  @Nested
  @DisplayName("Comparable Tests")
  class ComparableTests {
    @Test
    @DisplayName("compareTo() compares based on size")
    void compareTo_ComparesBySize() {
      CustomLinkedHashMap<Integer, String> smaller = new CustomLinkedHashMap<>();
      smaller.put(1, "A");
      CustomLinkedHashMap<Integer, String> larger = new CustomLinkedHashMap<>();
      larger.put(1, "A");
      larger.put(2, "B");
      larger.put(3, "C");
      larger.put(4, "D");

      assertTrue(map.compareTo(mapEmpty) > 0, "map should be greater than empty");
      assertTrue(mapEmpty.compareTo(map) < 0, "empty should be less than map");
      assertTrue(map.compareTo(smaller) > 0, "map should be greater than smaller");
      assertTrue(smaller.compareTo(map) < 0, "smaller should be less than map");
      assertTrue(map.compareTo(larger) < 0, "map should be less than larger");
      assertTrue(larger.compareTo(map) > 0, "larger should be greater than map");

      CustomLinkedHashMap<Integer, String> sameSize = new CustomLinkedHashMap<>();
      sameSize.put(10, "X");
      sameSize.put(20, "Y");
      sameSize.put(30, "Z");
      assertEquals(0, map.compareTo(sameSize), "Maps of same size should compare equal");
      assertEquals(0, sameSize.compareTo(map), "Maps of same size should compare equal");

      assertEquals(0, map.compareTo(map), "Map should compare equal to itself");
    }

    @Test
    @DisplayName("compareTo() throws NullPointerException for null argument")
    void compareTo_NullArgument_ThrowsNPE() {
      assertThrows(NullPointerException.class, () -> map.compareTo(null));
    }
  }

  // =====================================================
  // equals() and hashCode() Tests
  // =====================================================
  @Nested
  @DisplayName("equals() and hashCode() Tests")
  class EqualsHashCodeTests {

    @Test
    @DisplayName("equals() reflexive")
    void equals_Reflexive() {
      assertEquals(map, map, "Map should be equal to itself");
    }

    @Test
    @DisplayName("equals() symmetric")
    void equals_Symmetric() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      assertEquals(map, map2, "Maps with same elements and order should be equal");
      assertEquals(map2, map, "Equality should be symmetric");
    }

    @Test
    @DisplayName("equals() transitive")
    void equals_Transitive() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      CustomLinkedHashMap<Integer, String> map3 = createCopy(map);
      assertEquals(map, map2);
      assertEquals(map2, map3);
      assertEquals(map, map3, "Equality should be transitive");
    }

    @Test
    @DisplayName("equals() consistent")
    void equals_Consistent() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      assertEquals(map, map2);
      // No modifications, should still be equal
      assertEquals(map, map2);
    }

    @Test
    @DisplayName("equals() null returns false")
    void equals_Null() {
      assertNotEquals(null, map, "equals(null) should return false");
      assertNotEquals(null, map);
    }

    @Test
    @DisplayName("equals() different type returns false")
    void equals_DifferentType() {
      assertNotEquals("Not a Map", map);
      assertNotEquals(List.of(1, 2, 3), map);
    }

    @Test
    @DisplayName("equals() different size returns false")
    void equals_DifferentSize() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      map2.remove(K1);
      assertNotEquals(map, map2);
    }

    @Test
    @DisplayName("equals() different keys returns false")
    void equals_DifferentKeys() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      map2.remove(K3);
      map2.put(K_NON_EXIST, V3);
      assertNotEquals(map, map2);
    }

    @Test
    @DisplayName("equals() different values returns false")
    void equals_DifferentValues() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      map2.put(K3, V_NON_EXIST);
      assertNotEquals(map, map2);
    }

    @Test
    @DisplayName("equals() different order returns true (Map contract)")
    void equals_DifferentOrderReturnsTrue() {
      // Standard Map.equals contract ignores order
      CustomLinkedHashMap<Integer, String> mapOutOfOrder = new CustomLinkedHashMap<>();
      mapOutOfOrder.put(K3, V3);
      mapOutOfOrder.put(K1, V1);
      mapOutOfOrder.put(K2, V2);

      assertEquals(
          map,
          mapOutOfOrder,
          "Maps with same elements but different insertion order should be equal by Map contract");
      assertEquals(mapOutOfOrder, map);
    }

    @Test
    @DisplayName("hashCode() consistent")
    void hashCode_Consistent() {
      int hc1 = map.hashCode();
      int hc2 = map.hashCode();
      assertEquals(hc1, hc2, "hashCode should be consistent across calls");
    }

    @Test
    @DisplayName("hashCode() equals objects have equals hashCodes")
    void hashCode_EqualsObjects() {
      CustomLinkedHashMap<Integer, String> map2 = createCopy(map);
      assertEquals(map, map2, "Maps should be equal");
      assertEquals(map.hashCode(), map2.hashCode(), "Equal maps should have equal hashCodes");

      // Test with different order (should still be equal hashcode by Map contract)
      CustomLinkedHashMap<Integer, String> mapOutOfOrder = new CustomLinkedHashMap<>();
      mapOutOfOrder.put(K3, V3);
      mapOutOfOrder.put(K1, V1);
      mapOutOfOrder.put(K2, V2);
      assertEquals(map, mapOutOfOrder, "Maps should be equal regardless of order");
      assertEquals(
          map.hashCode(),
          mapOutOfOrder.hashCode(),
          "Equal maps (regardless of order) should have equal hashCodes");
    }

    @Test
    @DisplayName("hashCode() for empty map")
    void hashCode_EmptyMap() {
      assertEquals(0, mapEmpty.hashCode());
    }
  }

  // =====================================================
  // Cache Tests (Optional)
  // =====================================================
  @Nested
  @DisplayName("View Caching Tests")
  class CacheTests {

    @Test
    @DisplayName("Views should return same instance if cached")
    void views_ShouldReturnSameInstance() {
      SequencedSet<Integer> ks1 = map.keySet();
      SequencedSet<Integer> ks2 = map.keySet();
      assertSame(ks1, ks2, "keySet() should return cached instance");

      SequencedCollection<String> vs1 = map.values();
      SequencedCollection<String> vs2 = map.values();
      assertSame(vs1, vs2, "values() should return cached instance");

      SequencedSet<Map.Entry<Integer, String>> es1 = map.entrySet();
      SequencedSet<Map.Entry<Integer, String>> es2 = map.entrySet();
      assertSame(es1, es2, "entrySet() should return cached instance");

      SequencedMap<Integer, String> rm1 = map.reversed();
      SequencedMap<Integer, String> rm2 = map.reversed();
      assertSame(rm1, rm2, "reversed() should return cached instance");
    }

    @Test
    @DisplayName("Cache should be invalidated after clear()")
    void cache_ShouldBeInvalidatedAfterClear() {
      SequencedSet<Integer> ks1 = map.keySet();
      SequencedCollection<String> vs1 = map.values();
      SequencedSet<Map.Entry<Integer, String>> es1 = map.entrySet();
      SequencedMap<Integer, String> rm1 = map.reversed();

      map.clear(); // Clear should reset caches

      SequencedSet<Integer> ks2 = map.keySet();
      SequencedCollection<String> vs2 = map.values();
      SequencedSet<Map.Entry<Integer, String>> es2 = map.entrySet();
      SequencedMap<Integer, String> rm2 = map.reversed();

      assertNotSame(ks1, ks2, "keySet cache should be reset after clear");
      assertNotSame(vs1, vs2, "values cache should be reset after clear");
      assertNotSame(es1, es2, "entrySet cache should be reset after clear");
      assertNotSame(rm1, rm2, "reversed map cache should be reset after clear");

      assertTrue(ks2.isEmpty());
      assertTrue(vs2.isEmpty());
      assertTrue(es2.isEmpty());
      assertTrue(rm2.isEmpty());
    }
  }
}
