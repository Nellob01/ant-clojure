(ns ant-clojure.core
  (:require [clojure.java.io :as io])
  (:import [javafx.application Application]
           [javafx.fxml FXMLLoader]
           [javafx.scene Scene]
           [javafx.scene.paint Color]
           [javafx.animation AnimationTimer]
           [java.lang.Math])
  (:gen-class :extends javafx.application.Application))

(def width 800)
(def height 600)
(def ant-count 100)

(def ants (atom []))

(defn create-ants []
  (for [i (range ant-count)]
    {:x (rand-int width)
     :y (rand-int height)
     :color Color/BLACK}))

(defn draw-ants! [context]
  (.clearRect context 0 0 width height)
  (doseq [ant @ants]
    (.setFill context (:color ant))
    (.fillOval context (:x ant) (:y ant) 5 5)))

(defn random-step []
  (- (* 2 (rand)) 1))

(defn aggravate-ant [test-ant]
  (let [ant-array   @ants]
    (if (= 1 (get (frequencies
                         (for [ant ant-array]
                           (and (< (Math/abs (- (:x ant) (:x test-ant))) 10)
                                (< (Math/abs (- (:y ant) (:y test-ant))) 10)))) true))
        (assoc test-ant :color Color/BLACK)
        (assoc test-ant :color Color/RED))))

(defn move-ant [ant]
  (assoc ant
    :x (+ (random-step) (:x ant))
    :y (+ (random-step) (:y ant))))

;(defn move-ants []
;  (pmap move-ant @ants))

(def last-timestamp (atom 0))

(defn fps [now]
  (let [diff (- now @last-timestamp)
        diff-seconds (/ diff 1000000000)]
    (int (/ 1 diff-seconds))))

(defn -start [app stage]
  (let [root (javafx.fxml.FXMLLoader/load (io/resource "main.fxml"))
        scene (javafx.scene.Scene. root width height)
        canvas (.lookup scene "#canvas")
        context (.getGraphicsContext2D canvas)
        fps-label (.lookup scene "#fps")
        timer (proxy [javafx.animation.AnimationTimer] []
                (handle [now]
                  (.setText fps-label (str (fps now)))
                  (reset! last-timestamp now)
                  (reset! ants (doall (pmap aggravate-ant (pmap move-ant (deref ants)))))
                  (draw-ants! context)))]
    (.setTitle stage "Ants")
    (.setScene stage scene)
    (.show stage)
    (reset! ants (create-ants))
    (.start timer)))
        

(defn -main []
  (javafx.application.Application/launch ant_clojure.core (into-array String[])))
