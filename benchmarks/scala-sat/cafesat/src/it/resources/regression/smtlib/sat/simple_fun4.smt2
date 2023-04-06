(set-logic QF_UF)
(set-info :smt-lib-version 2.0)
(set-info :category "crafted")
(set-info :status sat)
(declare-sort U 0)
(declare-fun f (U) U)
(declare-fun g (U U) U)
(declare-fun a () U)
(declare-fun b () U)
(declare-fun c () U)
(assert (= (f a) (g a b)))
(assert (= (f a) c))
(assert (= (f b) c))
(assert (= (g (f b) b) (f b)))
(check-sat)