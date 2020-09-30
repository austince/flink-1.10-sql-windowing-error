package org.example.records;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.table.types.DataType;

import java.util.Arrays;
import java.util.List;

public class FieldDescriptor extends Tuple2<String, DataType> {
  private static final long serialVersionUID = 1L;

  public FieldDescriptor(String name, DataType type) {
    super(name, type);
  }

  public FieldDescriptor() {
    super();
  }

  public static FieldDescriptor of(String name, DataType type) {
    return new FieldDescriptor(name, type);
  }

  public static Schema toSchema(FieldDescriptor[] fields) {
    return toSchema(Arrays.asList(fields));
  }

  public static Schema toSchema(List<FieldDescriptor> fields) {
    Schema schema = new Schema();
    fields.forEach(field -> schema.field(field.f0, field.f1));
    return schema;
  }
}
