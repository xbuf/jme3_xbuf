[![Build Status](https://travis-ci.org/xbuf/jme3_xbuf.svg?branch=master)](https://travis-ci.org/xbuf/jme3_xbuf)

# jme3_xbuf

xbuf libs, tools, samples for jMonkeyEngine (jme3)

* **[jme3_xbuf_loader](https://github.com/xbuf/jme3_xbuf/tree/master/jme3_xbuf_loader)**: The library to load .xbuf data and to convert into jme model, material, skeleton, animation. The library is also required at runtime for data created from xbuf (for some Spatial's "Control")
* **[jme3_xbuf_remote](https://github.com/xbuf/jme3_xbuf/tree/master/jme3_xbuf_remote)**: The library to receive command and xbuf data over network (eg: from blender)
* **[jme3_xbuf_viewer](https://github.com/xbuf/jme3_xbuf/tree/master/jme3_xbuf_viewer)**: A simple pre-configured jME3 application to view xbuf data (until you integrate xbuf into your application or pipeline). It includes xbuf_remote + xbuf_loader + [davidB/jme3_ext_spatial_explorer](https://github.com/davidB/jme3_ext_spatial_explorer) (to have a javafx interface to explore the scene). *require java 8*
* **[samples/xbuf_jaime](https://github.com/xbuf/jme3_xbuf/tree/master/samples/xbuf_jaime)**: A sample application to compare Jaime load from j3o (test-data) or from xbuf (exported from .blend)

see [Get Started](https://github.com/xbuf/jme3_xbuf/wiki/Get-started)

# Download

Applications (like jme3_xbuf_viewer) are availables under the [releases](releases) page.

Libraries are availables at [bintray / jmonkeyengine / contrib](https://bintray.com/jmonkeyengine/contrib) (maven repo: https://dl.bintray.com/jmonkeyengine/contrib)

# Build

```
gradle publishToMavenLocal
```

# Screenshot (of the viewer)

![](http://i.imgur.com/MEV4AMa.png)
