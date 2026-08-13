# ============================================
# ECR Status Transition Validation
# Simulated Enovia/3DEXPERIENCE TCL Trigger
# ============================================

# Allowed status transitions
array set allowedTransitions {
    Draft     "InReview"
    InReview  "Approved Rejected"
    Rejected  "Draft"
    Approved  ""
}


# ============================================
# Validate Status Transition
# ============================================

proc validateTransition {currentStatus newStatus} {

    global allowedTransitions

    # Get allowed next states
    set allowedNextStates $allowedTransitions($currentStatus)

    # Check whether new status is allowed
    if {[lsearch -exact $allowedNextStates $newStatus] == -1} {

        puts "Invalid status transition: $currentStatus -> $newStatus"

        return 0
    }

    puts "Valid status transition: $currentStatus -> $newStatus"

    return 1
}


# ============================================
# Test Cases
# ============================================

puts "============================================"
puts "ECR Status Transition Tests"
puts "============================================"

validateTransition "Draft" "InReview"

validateTransition "InReview" "Approved"

validateTransition "InReview" "Rejected"

validateTransition "Rejected" "Draft"

validateTransition "Draft" "Approved"

validateTransition "Approved" "Draft"

puts "============================================"
puts "Tests completed"
puts "============================================"