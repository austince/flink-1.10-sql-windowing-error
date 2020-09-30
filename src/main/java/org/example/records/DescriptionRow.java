package org.example.records;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.flink.table.api.DataTypes;

import java.io.Serializable;

public class DescriptionRow implements Serializable {
  public static final String FILENAME = "descriptions.csv";

  public static final FieldDescriptor[] FIELDS = new FieldDescriptor[]{
      FieldDescriptor.of("uuid", DataTypes.STRING()),
      FieldDescriptor.of("description", DataTypes.STRING())
  };

  private static final long serialVersionUID = 1L;

  public String uuid;
  public String description;

  public DescriptionRow() {
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

    DescriptionRow other = (DescriptionRow) obj;
    return new EqualsBuilder()
        .append(uuid, other.uuid)
        .append(description, other.description)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(uuid)
        .append(description)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("uuid", uuid)
        .append("description", description)
        .toString();
  }
}
