;;; refresh.el ends here
;;; refresh.el --- description -*- lexical-binding: t; -*-
;;
;; Copyright (C) 2020 Eric Ihli
;;
;; Author: Eric Ihli <http://github/eihli>
;; Maintainer: Eric Ihli <eihli@owoga.com>
;; Created: December 09, 2020
;; Modified: December 09, 2020
;; Version: 0.0.1
;; Keywords:
;; Homepage: https://github.com/eihli/refresh
;; Package-Requires: ((emacs 27.1) (cl-lib "0.5"))
;;
;; This file is not part of GNU Emacs.
;;
;;; Commentary:
;;
;;  description
;;
;;; Code:

(setq cider-ns-refresh-before-fn "integrant.repl/suspend"
      cider-ns-refresh-after-fn "integrant.repl/resume")

(provide 'refresh)
;;; refresh.el ends here
