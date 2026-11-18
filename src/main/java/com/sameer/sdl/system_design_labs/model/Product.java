package com.sameer.sdl.system_design_labs.model;

import java.io.Serializable;

public record Product (long id, String name) implements Serializable {
}
