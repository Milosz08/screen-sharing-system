/*
 * Copyright (c) 2023 by MULTIPLE AUTHORS
 * Part of the CS study course project.
 */
package pl.polsl.screensharing.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavedConnDetailsDto implements Comparable<SavedConnDetailsDto> {
    private int id;
    private String ipAddress;
    private int port;
    private String username;
    private String description;

    @Override
    public int compareTo(SavedConnDetailsDto o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SavedConnDetailsDto that = (SavedConnDetailsDto) o;
        return port == that.port && Objects.equals(ipAddress, that.ipAddress)
            && Objects.equals(username, that.username) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return ipAddress.hashCode() * port * username.hashCode() * description.hashCode();
    }
}
