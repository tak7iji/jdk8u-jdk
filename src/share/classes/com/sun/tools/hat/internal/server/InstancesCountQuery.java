/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */


/* The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/, and in the file LICENSE.html in the
 * doc directory.
 *
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun. Portions created by Bill Foote and others
 * at Javasoft/Sun are Copyright (C) 1997-2004. All Rights Reserved.
 *
 * In addition to the formal license, I ask that you don't
 * change the history or donations files without permission.
 */

package com.sun.tools.hat.internal.server;

import com.sun.tools.hat.internal.model.*;
import com.sun.tools.hat.internal.util.ArraySorter;
import com.sun.tools.hat.internal.util.Comparer;
import java.util.Enumeration;

/**
 *
 * @author      Bill Foote
 */


class InstancesCountQuery extends QueryHandler {


    private boolean excludePlatform;

    public InstancesCountQuery(boolean excludePlatform) {
        this.excludePlatform = excludePlatform;
    }

    public void run() {
        if (excludePlatform) {
            startHtml("Instance Counts for All Classes (excluding platform)");
        } else {
            startHtml("Instance Counts for All Classes (including platform)");
        }

        JavaClass[] classes = snapshot.getClassesArray();
        if (excludePlatform) {
            int num = 0;
            for (int i = 0; i < classes.length; i++) {
                if (! PlatformClasses.isPlatformClass(classes[i])) {
                    classes[num++] = classes[i];
                }
            }
            JavaClass[] tmp = new JavaClass[num];
            System.arraycopy(classes, 0, tmp, 0, tmp.length);
            classes = tmp;
        }
        ArraySorter.sort(classes, new Comparer() {
            public int compare(Object lhso, Object rhso) {
                JavaClass lhs = (JavaClass) lhso;
                JavaClass rhs = (JavaClass) rhso;
                int diff = lhs.getInstancesCount(false)
                                - rhs.getInstancesCount(false);
                if (diff != 0) {
                    return -diff;       // Sort from biggest to smallest
                }
                String left = lhs.getName();
                String right = rhs.getName();
                if (left.startsWith("[") != right.startsWith("[")) {
                    // Arrays at the end
                    if (left.startsWith("[")) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return left.compareTo(right);
            }
        });

        String lastPackage = null;
        long totalSize = 0;
        long instances = 0;
        for (int i = 0; i < classes.length; i++) {
            JavaClass clazz = classes[i];
            int count = clazz.getInstancesCount(false);
            print("" + count);
            printAnchorStart();
            out.print("instances/" + encodeForURL(classes[i]));
            out.print("\"> ");
            if (count == 1) {
                print("instance");
            } else {
                print("instances");
            }
            out.print("</a> ");
            if (snapshot.getHasNewSet()) {
                Enumeration objects = clazz.getInstances(false);
                int newInst = 0;
                while (objects.hasMoreElements()) {
                    JavaHeapObject obj = (JavaHeapObject)objects.nextElement();
                    if (obj.isNew()) {
                        newInst++;
                    }
                }
                print("(");
                printAnchorStart();
                out.print("newInstances/" + encodeForURL(classes[i]));
                out.print("\">");
                print("" + newInst + " new");
                out.print("</a>) ");
            }
            print("of ");
            printClass(classes[i]);
            out.println("<br>");
            instances += count;
            totalSize += classes[i].getTotalInstanceSize();
        }
        out.println("<h2>Total of " + instances + " instances occupying " + totalSize + " bytes.</h2>");

        out.println("<h2>Other Queries</h2>");
        out.println("<ul>");

        out.print("<li>");
        printAnchorStart();
        if (!excludePlatform) {
            out.print("showInstanceCounts/\">");
            print("Show instance counts for all classes (excluding platform)");
        } else {
            out.print("showInstanceCounts/includePlatform/\">");
            print("Show instance counts for all classes (including platform)");
        }
        out.println("</a>");

        out.print("<li>");
        printAnchorStart();
        out.print("allClassesWithPlatform/\">");
        print("Show All Classes (including platform)");
        out.println("</a>");

        out.print("<li>");
        printAnchorStart();
        out.print("\">");
        print("Show All Classes (excluding platform)");
        out.println("</a>");

        out.println("</ul>");

        endHtml();
    }


}
