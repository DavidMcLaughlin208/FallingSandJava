package com.gdx.cellular.box2d.douglaspeucker;

import com.badlogic.gdx.math.Vector2;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecursiveApproach {

    private Vector2 sum(Vector2 v1, Vector2 v2) {
        return new Vector2(v1.x + v2.x, v1.y + v2.y);
    }

    private Vector2 diff(Vector2 v1, Vector2 v2) {
        return new Vector2(v1.x - v2.x, v1.y - v2.y);
    }

    private Vector2 prod(Vector2 v1, Vector2 v2) {
        return new Vector2(v1.x * v2.x, v1.y * v2.y);
    }

    private float dot(Vector2 v1, Vector2 v2) {
        return (v1.x * v2.x) + (v1.y * v2.y);
    }

    private float norm2(Vector2 v) {
        return (v.x * v.x) + (v.y * v.y);
    }

    private float norm(Vector2 v) {
        return (float) Math.sqrt(norm2(v));
    }

    private float d2(Vector2 v1, Vector2 v2) {
        return norm2(diff(v1,v2));
    }

    private float d(Vector2 v1, Vector2 v2) {
        return norm(diff(v1,v2));
    }

    public void simplifyDP(float tolerance, Vector2[] vertices, int startIndex, int endIndex, int[] markerBuffer) {
        if (startIndex <= endIndex + 1) { // there is nothing to simplify
            return;
        }
        // check for adequate approximation by segment S from v[j] to v[k]
        int maxi = startIndex;          // index of vertex farthest from S
        int maxd2 = 0;         // distance squared of farthest vertex
        float tol2 = tolerance * tolerance;  // tolerance squared
        Vector2[] segment = new Vector2[] {vertices[startIndex], vertices[endIndex]};  // segment from v[j] to v[k]
        Vector2 direction = diff(segment[1], segment[0]);   // segment direction vector
        float squaredSegmentLength = norm2(direction);     // segment length squared
        // test each vertex v[i] for max distance from S
        // compute using the Feb 2001 Algorithm's dist_Point_to_Segment()
        // Note: this works in any dimension (2D, 3D, ...)
        Vector2 w;           // vector
        Vector2 Pb;                // point, base of perpendicular from v[i] to S
        float b, cw, dv2;        // dv2 = distance v[i] to S squared
        for (int i=startIndex + 1; i < endIndex; i++) {
            // compute distance squared
            w = diff(vertices[i], segment[0]);
            cw = dot(w, direction);
            if ( cw <= 0 ) {
                dv2 = d2(vertices[i], segment[0]);
            } else if ( squaredSegmentLength <= cw ) {
                dv2 = d2(vertices[i], segment[1]);
            } else {
                b = cw / squaredSegmentLength;
                Pb = new Vector2(segment[0].x+b*direction.x, segment[0].y+b*direction.y);
                dv2 = d2(vertices[i], Pb);
            }
            // test with current max distance squared
            if (dv2 <= maxd2) {
                continue;
            }
            // v[i] is a new max vertex
            maxi = i;
            maxd2 = (int) dv2;
        }
        if (maxd2 > tol2) {      // error is worse than the tolerance
            // split the polyline at the farthest vertex from S
            markerBuffer[maxi] = 1;      // mark v[maxi] for the simplified polyline
            // recursively simplify the two subpolylines at v[maxi]
            simplifyDP( tolerance, vertices, startIndex, maxi, markerBuffer );  // polyline v[j] to v[maxi]
            simplifyDP( tolerance, vertices, maxi, endIndex, markerBuffer );  // polyline v[maxi] to v[k]
        }
        // else the approximation is OK, so ignore intermediate vertices
    }

    public List<Vector2> simplify(List<Vector2> vertices, float tolerance) {
        int n = vertices.size();
        Vector2[] savedVerts = new Vector2[n];
        int i, k, m, pv;               // misc counters
        float tol2 = tolerance * tolerance;          // tolerance squared
        Vector2[] vertexBuffer = new Vector2[n];                       // vertex buffer, points
        int[] markerBuffer = new int[vertices.size()];                       // marker buffer, ints

        // STAGE 1.  Vertex Reduction within tolerance of prior vertex cluster
        vertexBuffer[0] = vertices.get(0);              // start at the beginning
        for (i=k=1, pv=0; i<n; i++) {
            if (d2(vertices.get(i), vertices.get(pv)) < tol2) {
                continue;
            }
            vertexBuffer[k++] = vertices.get(i);
            pv = i;
        }
        if (pv < n-1) {
            vertexBuffer[k++] = vertices.get(n - 1);      // finish at the end
        }

        // STAGE 2.  Douglas-Peucker polyline simplification
        markerBuffer[0] = markerBuffer[k-1] = 1;       // mark the first and last vertices
        simplifyDP(tolerance, vertexBuffer, 0, k-1, markerBuffer);

        // copy marked vertices to the output simplified polyline
        for (i = m = 0; i < k; i++) {
            if (markerBuffer[i] != 0) {
                savedVerts[m++] = vertexBuffer[i];
            }
        }
        return Arrays.asList(savedVerts);
    }

//    var poly_simplify = function(, tol) {
//        // V ... [[x1,y1],[x2,y2],...] polyline
//        // tol  ... approximation tolerance
//        // ==============================================
//        // Copyright 2002, softSurfer (www.softsurfer.com)
//        // This code may be freely used and modified for any purpose
//        // providing that this copyright notice is included with it.
//        // SoftSurfer makes no warranty for this code, and cannot be held
//        // liable for any real or imagined damage resulting from its use.
//        // Users of this code must verify correctness for their application.
//        // http://softsurfer.com/Archive/algorithm_0205/algorithm_0205.htm
//        var sum = function(u,v) {return [u[0]+v[0], u[1]+v[1]];}
//        var diff = function(u,v) {return [u[0]-v[0], u[1]-v[1]];}
//        var prod = function(u,v) {return [u[0]*v[0], u[1]*v[1]];}
//        var dot = function(u,v) {return u[0]*v[0] + u[1]*v[1];}
//        var norm2 = function(v) {return v[0]*v[0] + v[1]*v[1];}
//        var norm = function(v) {return Math.sqrt(norm2(v));}
//        var d2 = function(u,v) {return norm2(diff(u,v));}
//        var d = function(u,v) {return norm(diff(u,v));}
//
//        var simplifyDP = function( tol, v, j, k, mk ) {
//            //  This is the Douglas-Peucker recursive simplification routine
//            //  It just marks vertices that are part of the simplified polyline
//            //  for approximating the polyline subchain v[j] to v[k].
//            //  mk[] ... array of markers matching vertex array v[]
//            if (k <= j+1) { // there is nothing to simplify
//                return;
//            }
//            // check for adequate approximation by segment S from v[j] to v[k]
//            var maxi = j;          // index of vertex farthest from S
//            var maxd2 = 0;         // distance squared of farthest vertex
//            var tol2 = tol * tol;  // tolerance squared
//            S = [v[j], v[k]];  // segment from v[j] to v[k]
//            u = diff(S[1], S[0]);   // segment direction vector
//            var cu = norm2(u,u);     // segment length squared
//            // test each vertex v[i] for max distance from S
//            // compute using the Feb 2001 Algorithm's dist_Point_to_Segment()
//            // Note: this works in any dimension (2D, 3D, ...)
//            var  w;           // vector
//            var Pb;                // point, base of perpendicular from v[i] to S
//            var b, cw, dv2;        // dv2 = distance v[i] to S squared
//            for (var i=j+1; i<k; i++) {
//                // compute distance squared
//                w = diff(v[i], S[0]);
//                cw = dot(w,u);
//                if ( cw <= 0 ) {
//                    dv2 = d2(v[i], S[0]);
//                } else if ( cu <= cw ) {
//                    dv2 = d2(v[i], S[1]);
//                } else {
//                    b = cw / cu;
//                    Pb = [S[0][0]+b*u[0], S[0][1]+b*u[1]];
//                    dv2 = d2(v[i], Pb);
//                }
//                // test with current max distance squared
//                if (dv2 <= maxd2) {
//                    continue;
//                }
//                // v[i] is a new max vertex
//                maxi = i;
//                maxd2 = dv2;
//            }
//            if (maxd2 > tol2) {      // error is worse than the tolerance
//                // split the polyline at the farthest vertex from S
//                mk[maxi] = 1;      // mark v[maxi] for the simplified polyline
//                // recursively simplify the two subpolylines at v[maxi]
//                simplifyDP( tol, v, j, maxi, mk );  // polyline v[j] to v[maxi]
//                simplifyDP( tol, v, maxi, k, mk );  // polyline v[maxi] to v[k]
//            }
//            // else the approximation is OK, so ignore intermediate vertices
//            return;
//        }
//
//        var n = V.length;
//        var sV = [];
//        var i, k, m, pv;               // misc counters
//        var tol2 = tol * tol;          // tolerance squared
//        vt = [];                       // vertex buffer, points
//        mk = [];                       // marker buffer, ints
//
//        // STAGE 1.  Vertex Reduction within tolerance of prior vertex cluster
//        vt[0] = V[0];              // start at the beginning
//        for (i=k=1, pv=0; i<n; i++) {
//            if (d2(V[i], V[pv]) < tol2) {
//                continue;
//            }
//            vt[k++] = V[i];
//            pv = i;
//        }
//        if (pv < n-1) {
//            vt[k++] = V[n-1];      // finish at the end
//        }
//
//        // STAGE 2.  Douglas-Peucker polyline simplification
//        mk[0] = mk[k-1] = 1;       // mark the first and last vertices
//        simplifyDP( tol, vt, 0, k-1, mk );
//
//        // copy marked vertices to the output simplified polyline
//        for (i=m=0; i<k; i++) {
//            if (mk[i]) {
//                sV[m++] = vt[i];
//            }
//        }
//        return sV;
//    }
}
