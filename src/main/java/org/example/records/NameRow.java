package org.example.records;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.flink.table.api.DataTypes;

import java.io.Serializable;

public class NameRow implements Serializable {
  public static final String FILENAME = "names.csv";

  public static final FieldDescriptor[] FIELDS = new FieldDescriptor[]{
      FieldDescriptor.of("uuid", DataTypes.STRING()),
      FieldDescriptor.of("name", DataTypes.STRING())
  };

  private static final long serialVersionUID = 1L;

  public String uuid;
  public String name;

  public NameRow() {
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    NameRow other = (NameRow) obj;
    return new EqualsBuilder()
        .append(uuid, other.uuid)
        .append(name, other.name)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(uuid)
        .append(name)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("uuid", uuid)
        .append("name", name)
        .toString();
  }
}
