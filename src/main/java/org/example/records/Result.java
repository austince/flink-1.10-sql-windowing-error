package org.example.records;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Result implements Serializable {
  private static final long serialVersionUID = 1L;

  public String uuid;
  public String name;
  public String description;

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

    Result other = (Result) obj;
    return new EqualsBuilder()
        .append(uuid, other.uuid)
        .append(name, other.name)
        .append(description, other.description)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(uuid)
        .append(name)
        .append(description)
        .toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("uuid", uuid)
        .append("name", name)
        .append("description", description)
        .toString();
  }
}
